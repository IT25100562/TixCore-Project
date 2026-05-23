package com.ticketbooking.controller;

import com.ticketbooking.model.Booking;
import com.ticketbooking.model.Event;
import com.ticketbooking.model.Review;
import com.ticketbooking.repository.*;
import com.ticketbooking.service.SeatService;
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

// Marks this class as a Spring MVC controller
@Controller

// Base URL mapping for all user application requests
@RequestMapping("/app")

public class UserAppController {

    // Automatically injects repository and service objects
    @Autowired private EventRepository eventRepo;
    @Autowired private VenueRepository venueRepo;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private ReviewRepository reviewRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private SeatService seatService;

    // Displays the home page with event listings
    @GetMapping("/home")
    public String home(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String category,
                       Model model) {

        // Retrieves and filters events
        List<Event> events = eventRepo.findAll().stream()

                // Shows only scheduled events
                .filter(e -> "scheduled".equalsIgnoreCase(e.getStatus()) || e.getStatus() == null || e.getStatus().isEmpty())

                // Filters events using search keyword
                .filter(e -> q == null || q.isBlank()
                        || e.getTitle().toLowerCase().contains(q.toLowerCase())
                        || (e.getDescription() != null && e.getDescription().toLowerCase().contains(q.toLowerCase())))

                // Filters events by category
                .filter(e -> category == null || category.isBlank() || category.equalsIgnoreCase(e.getCategory()))

                // Converts stream back into a list
                .collect(Collectors.toList());

        // Creates a map of venue IDs and venue names
        Map<String, String> venueNames = venueRepo.findAll().stream()
                .collect(Collectors.toMap(v -> v.getId(), v -> v.getName()));

        // Adds data to the model
        model.addAttribute("events", events);
        model.addAttribute("venueNames", venueNames);

        // Stores search and filter values
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("category", category == null ? "" : category);

        // Adds predefined event categories
        model.addAttribute("categories", List.of("Concert", "Conference", "Comedy", "Festival", "Networking", "Sports", "Theatre"));

        return "app/home";
    }

    // Displays detailed information about a specific event
    @GetMapping("/event/{id}")
    public String eventDetail(@PathVariable String id, Model model) {

        // Finds the event by ID
        Event ev = eventRepo.findById(id).orElse(null);

        if (ev == null) return "redirect:/app/home";

        // Adds event and venue details to the model
        model.addAttribute("event", ev);
        model.addAttribute("venue", venueRepo.findById(ev.getVenueId()).orElse(null));

        // Retrieves reviews related to the event
        List<Review> reviews = reviewRepo.findAll().stream()
                .filter(r -> id.equals(r.getEventId())).collect(Collectors.toList());

        // Creates a map of user IDs and display names
        Map<String, String> userNames = userRepo.findAll().stream()
                .collect(Collectors.toMap(u -> u.getId(), u -> u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getUsername()));

        // Adds reviews and usernames to the model
        model.addAttribute("reviews", reviews);
        model.addAttribute("userNames", userNames);

        return "app/event-detail";
    }

    // Handles ticket booking requests
    @PostMapping("/event/{id}/book")
    public String book(@PathVariable String id,
                       @RequestParam String seats,
                       HttpSession session,
                       Model model) {

        Event ev = eventRepo.findById(id).orElse(null);
        if (ev == null) return "redirect:/app/home";
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);

        // Counts the number of selected seats
        int seatCount = seats == null || seats.isBlank() ? 0 : seats.split(",").length;

        // Redirects if no seats are selected
        if (seatCount == 0) return "redirect:/app/event/" + id;

        Booking b = new Booking();

        // Stores booking information
        b.setUserId(userId);
        b.setEventId(id);
        b.setSeats(seats);
        // Calculates total booking price
        b.setTotalPrice(ev.getBasePrice() * seatCount);
        b.setStatus("confirmed");
        bookingRepo.save(b);

        return "redirect:/app/my-bookings";
    }

    // Handles review submission for events
    @PostMapping("/event/{id}/review")
    public String submitReview(@PathVariable String id,
                               @RequestParam int rating,
                               @RequestParam String comment,
                               HttpSession session) {

        if (eventRepo.findById(id).isEmpty()) return "redirect:/app/home";
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        Review r = new Review();

        // Stores review information
        r.setUserId(userId);
        r.setEventId(id);

        // Restricts rating between 1 and 5
        r.setRating(Math.max(1, Math.min(5, rating)));

        // Stores review comment with null safety check
        r.setComment(comment == null ? "" : comment);
        reviewRepo.save(r);

        return "redirect:/app/event/" + id;
    }

    // Displays the logged-in user's bookings
    @GetMapping("/my-bookings")
    public String myBookings(HttpSession session, Model model) {

        String userId = (String) session.getAttribute(SessionUtil.USER_ID);

        // Retrieves and filters bookings belonging to the current user
        List<Booking> mine = bookingRepo.findAll().stream()

                // Filters bookings by user ID
                .filter(b -> userId.equals(b.getUserId()))

                // Sorts bookings by newest first
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))

                // Converts stream back into a list
                .collect(Collectors.toList());

        // Creates a map of event IDs and event titles
        Map<String, String> eventTitles = eventRepo.findAll().stream()
                .collect(Collectors.toMap(e -> e.getId(), e -> e.getTitle()));

        // Adds bookings and event titles to the model
        model.addAttribute("bookings", mine);
        model.addAttribute("eventTitles", eventTitles);

        return "app/my-bookings";
    }

    // Handles booking cancellation requests
    @PostMapping("/booking/{id}/cancel")
    public String cancelBooking(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        bookingRepo.findById(id).ifPresent(b -> {

            // Ensures users can cancel only their own bookings
            if (userId.equals(b.getUserId())) {

                // Updates booking status
                b.setStatus("cancelled");
                bookingRepo.save(b);

                // Releases booked seats if seats exist
                if (b.getSeats() != null && !b.getSeats().isBlank()) {

                    // Converts seat string into a list
                    List<String> codes = java.util.Arrays.stream(b.getSeats().split(","))
                            .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

                    // Makes cancelled seats available again
                    seatService.releaseBookedSeats(b.getEventId(), codes);
                }
            }
        });

        // Redirects back to booking history page
        return "redirect:/app/my-bookings";
    }
}
