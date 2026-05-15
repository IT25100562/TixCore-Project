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
    @Autowired private UserRepository userRepo;
    @Autowired private AdminRepository adminRepo;
    @Autowired private VenueRepository venueRepo;
    @Autowired private EventRepository eventRepo;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private ReviewRepository reviewRepo;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userRepo.findAll().size());
        model.addAttribute("totalAdmins", adminRepo.findAll().size());
        model.addAttribute("totalVenues", venueRepo.findAll().size());
        model.addAttribute("totalEvents", eventRepo.findAll().size());
        model.addAttribute("totalBookings", bookingRepo.findAll().size());
        model.addAttribute("totalReviews", reviewRepo.findAll().size());
        double revenue = bookingRepo.findAll().stream()
            .filter(b -> !"cancelled".equalsIgnoreCase(b.getStatus()))
            .mapToDouble(b -> b.getTotalPrice()).sum();
        model.addAttribute("totalRevenue", revenue);
        double avgRating = reviewRepo.findAll().stream()
            .mapToInt(r -> r.getRating()).average().orElse(0.0);
        model.addAttribute("averageRating", Math.round(avgRating * 10.0) / 10.0);
        model.addAttribute("recentEvents", eventRepo.findAll().stream()
            .sorted((a,b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(5).toList());

        Map<String, Long> categoryCounts = eventRepo.findAll().stream()
            .filter(e -> e.getCategory() != null && !e.getCategory().isBlank())
            .collect(Collectors.groupingBy(e -> e.getCategory(), Collectors.counting()));
        List<String> categoryLabels = new ArrayList<>(categoryCounts.keySet());
        List<Long> categoryValues = categoryLabels.stream().map(categoryCounts::get).collect(Collectors.toList());
        model.addAttribute("categoryLabels", categoryLabels);
        model.addAttribute("categoryValues", categoryValues);
        return "dashboard";
    }
}
