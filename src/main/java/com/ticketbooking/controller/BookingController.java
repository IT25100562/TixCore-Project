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

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired private BookingRepository repo;
    @Autowired private UserRepository userRepo;
    @Autowired private EventRepository eventRepo;
    @Autowired private SeatService seatService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("bookings", repo.findAll());
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("events", eventRepo.findAll());
        return "bookings/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        Booking b = new Booking();
        b.setStatus("confirmed");
        model.addAttribute("booking", b);
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("events", eventRepo.findAll());
        return "bookings/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        model.addAttribute("booking", repo.findById(id).orElse(new Booking()));
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("events", eventRepo.findAll());
        return "bookings/form";
    }

    @PostMapping
    public String save(@ModelAttribute Booking booking) {
        if ("cancelled".equalsIgnoreCase(booking.getStatus())) {
            repo.findById(booking.getId()).ifPresent(old -> {
                if (!"cancelled".equalsIgnoreCase(old.getStatus())
                        && old.getSeats() != null && !old.getSeats().isBlank()
                        && old.getEventId() != null) {
                    List<String> codes = Arrays.stream(old.getSeats().split(","))
                            .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                    seatService.releaseBookedSeats(old.getEventId(), codes);
                }
            });
        }
        repo.save(booking);
        return "redirect:/bookings";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        repo.findById(id).ifPresent(b -> {
            if (b.getSeats() != null && !b.getSeats().isBlank() && b.getEventId() != null) {
                List<String> codes = Arrays.stream(b.getSeats().split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                seatService.releaseBookedSeats(b.getEventId(), codes);
            }
            repo.deleteById(id);
        });
        return "redirect:/bookings";
    }
}
