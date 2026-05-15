package com.ticketbooking.controller;

import com.ticketbooking.model.Booking;
import com.ticketbooking.model.Event;
import com.ticketbooking.model.Review;
import com.ticketbooking.repository.*;
import com.ticketbooking.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/app")
public class UserAppController {

    @Autowired private EventRepository eventRepo;
    @Autowired private VenueRepository venueRepo;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private ReviewRepository reviewRepo;
    @Autowired private UserRepository userRepo;

    @GetMapping("/home")
    public String home(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String category,
                       Model model) {
        List<Event> events = eventRepo.findAll().stream()
            .filter(e -> "scheduled".equalsIgnoreCase(e.getStatus()) || e.getStatus() == null || e.getStatus().isEmpty())
            .filter(e -> q == null || q.isBlank()
                || e.getTitle().toLowerCase().contains(q.toLowerCase())
                || (e.getDescription() != null && e.getDescription().toLowerCase().contains(q.toLowerCase())))
            .filter(e -> category == null || category.isBlank() || category.equalsIgnoreCase(e.getCategory()))
            .collect(Collectors.toList());
        Map<String, String> venueNames = venueRepo.findAll().stream()
            .collect(Collectors.toMap(v -> v.getId(), v -> v.getName()));
        model.addAttribute("events", events);
        model.addAttribute("venueNames", venueNames);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("category", category == null ? "" : category);
        model.addAttribute("categories", List.of("Concert", "Conference", "Comedy", "Festival", "Networking", "Sports", "Theatre"));
        return "app/home";
    }

    @GetMapping("/event/{id}")
    public String eventDetail(@PathVariable String id, Model model) {
        Event ev = eventRepo.findById(id).orElse(null);
        if (ev == null) return "redirect:/app/home";
        model.addAttribute("event", ev);
        model.addAttribute("venue", venueRepo.findById(ev.getVenueId()).orElse(null));
        List<Review> reviews = reviewRepo.findAll().stream()
            .filter(r -> id.equals(r.getEventId())).collect(Collectors.toList());
        Map<String, String> userNames = userRepo.findAll().stream()
            .collect(Collectors.toMap(u -> u.getId(), u -> u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getUsername()));
        model.addAttribute("reviews", reviews);
        model.addAttribute("userNames", userNames);
        return "app/event-detail";
    }

    @PostMapping("/event/{id}/book")
    public String book(@PathVariable String id,
                       @RequestParam String seats,
                       HttpSession session,
                       Model model) {
        Event ev = eventRepo.findById(id).orElse(null);
        if (ev == null) return "redirect:/app/home";
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        int seatCount = seats == null || seats.isBlank() ? 0 : seats.split(",").length;
        if (seatCount == 0) return "redirect:/app/event/" + id;
        Booking b = new Booking();
        b.setUserId(userId);
        b.setEventId(id);
        b.setSeats(seats);
        b.setTotalPrice(ev.getBasePrice() * seatCount);
        b.setStatus("confirmed");
        bookingRepo.save(b);
        return "redirect:/app/my-bookings";
    }

    @PostMapping("/event/{id}/review")
    public String submitReview(@PathVariable String id,
                               @RequestParam int rating,
                               @RequestParam String comment,
                               HttpSession session) {
        if (eventRepo.findById(id).isEmpty()) return "redirect:/app/home";
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        Review r = new Review();
        r.setUserId(userId);
        r.setEventId(id);
        r.setRating(Math.max(1, Math.min(5, rating)));
        r.setComment(comment == null ? "" : comment);
        reviewRepo.save(r);
        return "redirect:/app/event/" + id;
    }

    @GetMapping("/my-bookings")
    public String myBookings(HttpSession session, Model model) {
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        List<Booking> mine = bookingRepo.findAll().stream()
            .filter(b -> userId.equals(b.getUserId()))
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(Collectors.toList());
        Map<String, String> eventTitles = eventRepo.findAll().stream()
            .collect(Collectors.toMap(e -> e.getId(), e -> e.getTitle()));
        model.addAttribute("bookings", mine);
        model.addAttribute("eventTitles", eventTitles);
        return "app/my-bookings";
    }

    @PostMapping("/booking/{id}/cancel")
    public String cancelBooking(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        bookingRepo.findById(id).ifPresent(b -> {
            if (userId.equals(b.getUserId())) {
                b.setStatus("cancelled");
                bookingRepo.save(b);
            }
        });
        return "redirect:/app/my-bookings";
    }
}
