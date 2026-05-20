package com.ticketbooking.controller;

import com.ticketbooking.model.Booking;
import com.ticketbooking.model.Event;
import com.ticketbooking.model.Seat;
import com.ticketbooking.repository.BookingRepository;
import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.service.SeatService;
import com.ticketbooking.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SeatApiController {

    @Autowired private SeatService seatService;
    @Autowired private EventRepository eventRepo;
    @Autowired private BookingRepository bookingRepo;

    @GetMapping("/events/{eventId}/seats")
    public ResponseEntity<?> listSeats(@PathVariable String eventId, HttpSession session) {
        Event ev = eventRepo.findById(eventId).orElse(null);
        if (ev == null) return ResponseEntity.notFound().build();
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        long now = System.currentTimeMillis();

        List<Seat> seats = seatService.getSeatsForEvent(eventId);
        List<Map<String, Object>> seatDtos = seats.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("row", s.getRowLabel());
            m.put("number", s.getSeatNumber());
            m.put("code", s.getCode());
            String status = s.getStatus();
            // Show "MINE" for current user's locks so UI keeps them selected
            if (Seat.LOCKED.equals(status)) {
                if (s.getLockedUntil() < now) status = Seat.AVAILABLE;
                else if (userId != null && userId.equals(s.getLockedBy())) status = "MINE";
            }
            m.put("status", status);
            return m;
        }).sorted(Comparator
                .comparing((Map<String, Object> m) -> (String) m.get("row"))
                .thenComparingInt(m -> (Integer) m.get("number")))
          .collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("eventId", eventId);
        body.put("rows", SeatService.ROWS);
        body.put("seatsPerRow", SeatService.SEATS_PER_ROW);
        body.put("basePrice", ev.getBasePrice());
        body.put("seats", seatDtos);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/events/{eventId}/lock")
    public ResponseEntity<?> lock(@PathVariable String eventId,
                                  @RequestBody Map<String, Object> body,
                                  HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        @SuppressWarnings("unchecked")
        List<String> seatIds = (List<String>) body.getOrDefault("seatIds", List.of());
        if (seatIds.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "No seats selected"));
        long until = seatService.lockSeats(eventId, seatIds, userId);
        if (until < 0) return ResponseEntity.status(409).body(Map.of("error", "One or more seats unavailable"));
        return ResponseEntity.ok(Map.of("lockedUntil", until, "expiresInSec", (until - System.currentTimeMillis()) / 1000));
    }

    @PostMapping("/events/{eventId}/book")
    public ResponseEntity<?> book(@PathVariable String eventId,
                                  @RequestBody Map<String, Object> body,
                                  HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        Event ev = eventRepo.findById(eventId).orElse(null);
        if (ev == null) return ResponseEntity.notFound().build();
        @SuppressWarnings("unchecked")
        List<String> seatIds = (List<String>) body.getOrDefault("seatIds", List.of());
        if (seatIds.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "No seats selected"));

        boolean ok = seatService.confirmBooking(eventId, seatIds, userId);
        if (!ok) return ResponseEntity.status(409).body(Map.of("error", "Seat lock expired or seats taken. Please re-select."));

        List<Seat> seats = seatService.findByIds(seatIds);
        String codes = seats.stream()
                .sorted(Comparator.comparing(Seat::getRowLabel).thenComparingInt(Seat::getSeatNumber))
                .map(Seat::getCode).collect(Collectors.joining(","));

        Booking b = new Booking();
        b.setUserId(userId);
        b.setEventId(eventId);
        b.setSeats(codes);
        b.setTotalPrice(ev.getBasePrice() * seats.size());
        b.setStatus("confirmed");
        bookingRepo.save(b);

        return ResponseEntity.ok(Map.of(
                "bookingId", b.getId(),
                "seats", codes,
                "totalPrice", b.getTotalPrice(),
                "redirect", "/app/my-bookings"
        ));
    }

    @PostMapping("/events/{eventId}/release")
    public ResponseEntity<?> release(@PathVariable String eventId, HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        seatService.releaseUserLocks(eventId, userId);
        return ResponseEntity.ok(Map.of("released", true));
    }
}
