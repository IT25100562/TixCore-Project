package com.ticketbooking.controller;

import com.ticketbooking.model.Event;
import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Controller
@RequestMapping("/events")
public class EventController {

    @Autowired private EventRepository repo;
    @Autowired private VenueRepository venueRepo;

    // Show all events
    @GetMapping
    public String list(Model model) {
        model.addAttribute("events", repo.findAll());
        model.addAttribute("venues", venueRepo.findAll());
        return "events/list";
    }

    // Show form to create a new event
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("event", new Event());
        model.addAttribute("venues", venueRepo.findAll());
        return "events/form";
    }

    // Show form to edit an existing event
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        model.addAttribute("event", repo.findById(id).orElse(new Event()));
        model.addAttribute("venues", venueRepo.findAll());
        return "events/form";
    }

    // Save new or updated event
    @PostMapping
    public String save(@ModelAttribute Event event, Model model) {
        boolean isNew = event.getId() == null || event.getId().isBlank();

        // Reject past dates for new events
        if (isNew && event.getDate() != null && !event.getDate().isBlank()) {
            try {
                if (LocalDate.parse(event.getDate()).isBefore(LocalDate.now())) {
                    model.addAttribute("dateError",
                            "Event date cannot be in the past. Please select today or a future date.");
                    model.addAttribute("venues", venueRepo.findAll());
                    return "events/form";
                }
            } catch (Exception ignored) {}
        }

        repo.save(event);
        return "redirect:/events";
    }

    // Delete event by ID
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        repo.deleteById(id);
        return "redirect:/events";
    }
}