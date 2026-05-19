package com.ticketbooking.controller;

import com.ticketbooking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired private UserRepository    userRepo;
    @Autowired private AdminRepository   adminRepo;
    @Autowired private VenueRepository   venueRepo;
    @Autowired private EventRepository   eventRepo;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private ReviewRepository  reviewRepo;

    @GetMapping("/")
    public String dashboard(Model model) {

        var allBookings = bookingRepo.findAll();
        var allEvents   = eventRepo.findAll();
        var allUsers    = userRepo.findAll();

        // ── Core counts ──────────────────────────────────────────────
        long confirmedCount = allBookings.stream()
                .filter(b -> "confirmed".equalsIgnoreCase(b.getStatus())).count();
        long cancelledCount = allBookings.stream()
                .filter(b -> "cancelled".equalsIgnoreCase(b.getStatus())).count();

        model.addAttribute("totalUsers",     allUsers.size());
        model.addAttribute("totalAdmins",    adminRepo.findAll().size());
        model.addAttribute("totalVenues",    venueRepo.findAll().size());
        model.addAttribute("totalEvents",    allEvents.size());
        model.addAttribute("totalBookings",  allBookings.size());
        model.addAttribute("confirmedBookings", confirmedCount);
        model.addAttribute("cancelledBookings", cancelledCount);
        model.addAttribute("totalReviews",   reviewRepo.findAll().size());

        double revenue = allBookings.stream()
                .filter(b -> !"cancelled".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(b -> b.getTotalPrice()).sum();
        model.addAttribute("totalRevenue", revenue);

        double avgRating = reviewRepo.findAll().stream()
                .mapToInt(r -> r.getRating()).average().orElse(0.0);
        model.addAttribute("averageRating", Math.round(avgRating * 10.0) / 10.0);

        // ── Events by category (bar chart) ───────────────────────────
        Map<String, Long> categoryCounts = allEvents.stream()
                .filter(e -> e.getCategory() != null && !e.getCategory().isBlank())
                .collect(Collectors.groupingBy(e -> e.getCategory(), Collectors.counting()));
        List<String> categoryLabels = new ArrayList<>(categoryCounts.keySet());
        List<Long>   categoryValues = categoryLabels.stream()
                .map(categoryCounts::get).collect(Collectors.toList());
        model.addAttribute("categoryLabels", categoryLabels);
        model.addAttribute("categoryValues", categoryValues);

        // ── Revenue by month (line chart) ────────────────────────────
        Map<String, Double> revenueMap = new TreeMap<>();
        allBookings.stream()
                .filter(b -> !"cancelled".equalsIgnoreCase(b.getStatus())
                        && b.getCreatedAt() != null && b.getCreatedAt().length() >= 7)
                .forEach(b -> revenueMap.merge(
                        b.getCreatedAt().substring(0, 7), b.getTotalPrice(), Double::sum));
        model.addAttribute("revenueLabels", new ArrayList<>(revenueMap.keySet()));
        model.addAttribute("revenueValues", new ArrayList<>(revenueMap.values()));

        // ── Booking status (doughnut chart) ──────────────────────────
        model.addAttribute("statusLabels", List.of("Confirmed", "Cancelled"));
        model.addAttribute("statusValues", List.of(confirmedCount, cancelledCount));

        // ── Top 5 events by booking count + revenue ──────────────────
        Map<String, String> eventTitles = allEvents.stream()
                .collect(Collectors.toMap(e -> e.getId(), e -> e.getTitle()));
        Map<String, Long>   eventBkCount  = new HashMap<>();
        Map<String, Double> eventBkRevenue = new HashMap<>();
        allBookings.stream()
                .filter(b -> !"cancelled".equalsIgnoreCase(b.getStatus())
                        && b.getEventId() != null)
                .forEach(b -> {
                    eventBkCount.merge(b.getEventId(),  1L, Long::sum);
                    eventBkRevenue.merge(b.getEventId(), b.getTotalPrice(), Double::sum);
                });
        List<Map<String, Object>> topEvents = eventBkCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("title",    eventTitles.getOrDefault(entry.getKey(), "Unknown"));
                    m.put("bookings", entry.getValue());
                    m.put("revenue",  eventBkRevenue.getOrDefault(entry.getKey(), 0.0));
                    return m;
                })
                .collect(Collectors.toList());
        model.addAttribute("topEvents", topEvents);

        // ── Recent 8 bookings ────────────────────────────────────────
        Map<String, String> userNames = allUsers.stream()
                .collect(Collectors.toMap(u -> u.getId(),
                        u -> u.getFullName() != null && !u.getFullName().isBlank()
                                ? u.getFullName() : u.getUsername()));
        List<Map<String, Object>> recentBookings = allBookings.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(8)
                .map(b -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("user",   userNames.getOrDefault(b.getUserId(), b.getUserId()));
                    m.put("event",  eventTitles.getOrDefault(b.getEventId(), "Unknown"));
                    m.put("seats",  b.getSeats());
                    m.put("total",  b.getTotalPrice());
                    m.put("status", b.getStatus());
                    String date = b.getCreatedAt();
                    m.put("date",   date != null && date.length() >= 10 ? date.substring(0, 10) : date);
                    return m;
                })
                .collect(Collectors.toList());
        model.addAttribute("recentBookings", recentBookings);

        // ── Recent 5 events ──────────────────────────────────────────
        model.addAttribute("recentEvents", allEvents.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5).toList());

        return "dashboard";
    }
}
