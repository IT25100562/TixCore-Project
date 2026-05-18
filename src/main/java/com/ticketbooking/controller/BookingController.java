package com.ticketbooking.controller;

import com.ticketbooking.model.Booking;
import com.ticketbooking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bookings")
public class BookingController {
    @Autowired private BookingRepository repo;
    @Autowired private UserRepository userRepo;
    @Autowired private EventRepository eventRepo;

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
        repo.save(booking);
        return "redirect:/bookings";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        repo.deleteById(id);
        return "redirect:/bookings";
    }
}
