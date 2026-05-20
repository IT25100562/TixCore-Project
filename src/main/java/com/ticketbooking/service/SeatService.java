package com.ticketbooking.service;

import com.ticketbooking.model.Booking;
import com.ticketbooking.model.Seat;
import com.ticketbooking.repository.BookingRepository;
import com.ticketbooking.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SeatService {

    public static final int ROWS = 8;          // A..H
    public static final int SEATS_PER_ROW = 12;
    public static final long LOCK_DURATION_MS = 5 * 60 * 1000L; // 5 minutes

    @Autowired private SeatRepository seatRepo;
    @Autowired private BookingRepository bookingRepo;

    /** Returns seats for an event, lazily generating them on first access.
     *  Pre-existing confirmed bookings are reconciled so their seats show as BOOKED. */
    public synchronized List<Seat> getSeatsForEvent(String eventId) {
        List<Seat> existing = seatRepo.findAll().stream()
                .filter(s -> eventId.equals(s.getEventId()))
                .collect(Collectors.toList());
        if (!existing.isEmpty()) {
            List<Seat> refreshed = refreshExpiredLocks(existing);
            reconcileBookings(eventId, refreshed);
            return seatRepo.findAll().stream()
                    .filter(s -> eventId.equals(s.getEventId()))
                    .collect(Collectors.toList());
        }
        // Generate fresh seat map
        List<Seat> created = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            String row = String.valueOf((char) ('A' + r));
            for (int n = 1; n <= SEATS_PER_ROW; n++) {
                Seat s = new Seat();
                s.setEventId(eventId);
                s.setRowLabel(row);
                s.setSeatNumber(n);
                s.setStatus(Seat.AVAILABLE);
                seatRepo.save(s);
                created.add(s);
            }
        }
        // Immediately mark seats from confirmed bookings as BOOKED
        reconcileBookings(eventId, created);
        return seatRepo.findAll().stream()
                .filter(s -> eventId.equals(s.getEventId()))
                .collect(Collectors.toList());
    }

    /**
     * Scans all non-cancelled bookings for this event and marks their seat codes
     * as BOOKED in the seat map (if they are currently AVAILABLE).
     * This ensures pre-seeded / imported bookings are always reflected correctly.
     */
    private void reconcileBookings(String eventId, List<Seat> seats) {
        Set<String> bookedCodes = bookingRepo.findAll().stream()
                .filter(b -> eventId.equals(b.getEventId())
                        && !"cancelled".equalsIgnoreCase(b.getStatus())
                        && b.getSeats() != null && !b.getSeats().isBlank())
                .flatMap(b -> Arrays.stream(b.getSeats().split(",")))
                .map(String::trim)
                .filter(c -> !c.isEmpty())
                .collect(Collectors.toSet());

        if (bookedCodes.isEmpty()) return;

        for (Seat s : seats) {
            if (bookedCodes.contains(s.getCode()) && !Seat.BOOKED.equals(s.getStatus())) {
                s.setStatus(Seat.BOOKED);
                s.setLockedUntil(0);
                s.setLockedBy("");
                seatRepo.save(s);
            }
        }
    }

    /** If a lock has expired, revert it to AVAILABLE. */
    private List<Seat> refreshExpiredLocks(List<Seat> seats) {
        long now = System.currentTimeMillis();
        boolean changed = false;
        for (Seat s : seats) {
            if (Seat.LOCKED.equals(s.getStatus()) && s.getLockedUntil() > 0 && s.getLockedUntil() < now) {
                s.setStatus(Seat.AVAILABLE);
                s.setLockedUntil(0);
                s.setLockedBy("");
                seatRepo.save(s);
                changed = true;
            }
        }
        if (changed) {
            return seatRepo.findAll().stream()
                    .filter(s -> seats.get(0).getEventId().equals(s.getEventId()))
                    .collect(Collectors.toList());
        }
        return seats;
    }

    public synchronized List<Seat> findByIds(List<String> ids) {
        return seatRepo.findAll().stream()
                .filter(s -> ids.contains(s.getId()))
                .collect(Collectors.toList());
    }

    /** Lock seats for the user. Returns expiry timestamp, or -1 on failure. */
    public synchronized long lockSeats(String eventId, List<String> seatIds, String userId) {
        long now = System.currentTimeMillis();
        long until = now + LOCK_DURATION_MS;
        List<Seat> all = getSeatsForEvent(eventId);

        // Validate: each must belong to event, and be AVAILABLE or already locked by this user
        for (String id : seatIds) {
            Seat s = all.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
            if (s == null) return -1;
            if (Seat.BOOKED.equals(s.getStatus())) return -1;
            if (Seat.LOCKED.equals(s.getStatus())
                    && s.getLockedUntil() > now
                    && !userId.equals(s.getLockedBy())) {
                return -1;
            }
        }

        // Release any other seats currently locked by this user for this event but not in this set
        for (Seat s : all) {
            if (Seat.LOCKED.equals(s.getStatus()) && userId.equals(s.getLockedBy())
                    && !seatIds.contains(s.getId())) {
                s.setStatus(Seat.AVAILABLE);
                s.setLockedUntil(0);
                s.setLockedBy("");
                seatRepo.save(s);
            }
        }

        // Apply locks
        for (String id : seatIds) {
            Seat s = all.stream().filter(x -> x.getId().equals(id)).findFirst().get();
            s.setStatus(Seat.LOCKED);
            s.setLockedUntil(until);
            s.setLockedBy(userId);
            seatRepo.save(s);
        }
        return until;
    }

    /** Marks seats as BOOKED. Returns true on success. */
    public synchronized boolean confirmBooking(String eventId, List<String> seatIds, String userId) {
        long now = System.currentTimeMillis();
        List<Seat> all = getSeatsForEvent(eventId);
        // Validate
        for (String id : seatIds) {
            Seat s = all.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
            if (s == null) return false;
            if (Seat.BOOKED.equals(s.getStatus())) return false;
            if (Seat.LOCKED.equals(s.getStatus())) {
                if (s.getLockedUntil() < now) return false;
                if (!userId.equals(s.getLockedBy())) return false;
            }
        }
        // Mark booked
        for (String id : seatIds) {
            Seat s = all.stream().filter(x -> x.getId().equals(id)).findFirst().get();
            s.setStatus(Seat.BOOKED);
            s.setLockedUntil(0);
            s.setLockedBy("");
            seatRepo.save(s);
        }
        return true;
    }

    /** Release all of a user's locks on an event (e.g., on cancel). */
    public synchronized void releaseUserLocks(String eventId, String userId) {
        for (Seat s : getSeatsForEvent(eventId)) {
            if (Seat.LOCKED.equals(s.getStatus()) && userId.equals(s.getLockedBy())) {
                s.setStatus(Seat.AVAILABLE);
                s.setLockedUntil(0);
                s.setLockedBy("");
                seatRepo.save(s);
            }
        }
    }

    /** When a booking is cancelled/deleted, free its booked seats by code. */
    public synchronized void releaseBookedSeats(String eventId, List<String> codes) {
        for (Seat s : getSeatsForEvent(eventId)) {
            if (codes.contains(s.getCode()) && Seat.BOOKED.equals(s.getStatus())) {
                s.setStatus(Seat.AVAILABLE);
                seatRepo.save(s);
            }
        }
    }
}
