package com.ticketbooking.controller;

import com.ticketbooking.model.Booking;
import com.ticketbooking.repository.*;
import com.ticketbooking.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller // Marks this class as a Spring MVC Controller handling web requests.
@RequestMapping("/bookings") // Maps all requests starting with "/bookings" to this controller.
public class BookingController {

    //  RELATIONSHIPS: Controller depends on Repositories and Services via Dependency Injection 
    @Autowired private BookingRepository repo;
    @Autowired private UserRepository userRepo;
    @Autowired private EventRepository eventRepo;
    @Autowired private SeatService seatService;

    @GetMapping // Handles get requests to /bookings (Read operation).
    public String list(Model model) {
        model.addAttribute("bookings", repo.findAll());
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("events", eventRepo.findAll());
        return "bookings/list"; // Returns the html view template named "list" inside the "bookings" folder.
    }

    @GetMapping("/new") // Handles get requests to /bookings/new to show the form for a new booking.
    public String createForm(Model model) {
        Booking b = new Booking();
        b.setStatus("confirmed");
        model.addAttribute("booking", b);
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("events", eventRepo.findAll());
        return "bookings/form";
    }

    @GetMapping("/{id}/edit") // Handles get requests to edit an existing booking. Path variable captures the ID.
    public String editForm(@PathVariable String id, Model model) {
        // Finds the booking by ID. If not found, creates a new one (Error prevention/fallback).
        model.addAttribute("booking", repo.findById(id).orElse(new Booking())); 
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("events", eventRepo.findAll());
        return "bookings/form";
    }

    @PostMapping // Handles post requests to "/bookings" (Create/Update operation). Post mapping concept.
    public String save(@ModelAttribute Booking booking) {
        if ("cancelled".equalsIgnoreCase(booking.getStatus())) { // Validation: Checks if the booking is being cancelled.
            repo.findById(booking.getId()).ifPresent(old -> {
                if (!"cancelled".equalsIgnoreCase(old.getStatus())
                        && old.getSeats() != null && !old.getSeats().isBlank()
                        && old.getEventId() != null) {
                    // Splits the comma separated seat string, trims spaces, and collects to a List.
                    List<String> codes = Arrays.stream(old.getSeats().split(","))
                            .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                    seatService.releaseBookedSeats(old.getEventId(), codes);
                }
            });
        }
        repo.save(booking); // Saves the new or updated booking object to the text file via repository.
        return "redirect:/bookings";
    }

    @PostMapping("/{id}/delete") // Handles POST requests to delete a booking.
    public String delete(@PathVariable String id) {
        repo.findById(id).ifPresent(b -> { // Finds the booking first to release its seats before deleting.
            if (b.getSeats() != null && !b.getSeats().isBlank() && b.getEventId() != null) { // Validation check.
                // Converts comma separated seats into a list.
                List<String> codes = Arrays.stream(b.getSeats().split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                seatService.releaseBookedSeats(b.getEventId(), codes);
            }
            repo.deleteById(id);
        });
        return "redirect:/bookings";
    }
}