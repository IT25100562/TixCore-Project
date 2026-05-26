package com.ticketbooking.controller;

// Import Event model (represents event data)
import com.ticketbooking.model.Event;

// Import repository to handle event data storage
import com.ticketbooking.repository.EventRepository;

// Import repository for venue (event location)
import com.ticketbooking.repository.VenueRepository;

// Spring Boot annotations for dependency injection and controller
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

// Used to send data from controller to HTML views
import org.springframework.ui.Model;

// Used for handling HTTP requests
import org.springframework.web.bind.annotation.*;

// Used to work with date values
import java.time.LocalDate;

// Marks this class as a Spring MVC Controller
@Controller

// Base URL mapping for all methods in this controller
@RequestMapping("/events")
public class EventController {

    // Automatically inject EventRepository object
    @Autowired private EventRepository repo;

    // Automatically inject VenueRepository object
    @Autowired private VenueRepository venueRepo;

    // Handles GET request to /events
    // Purpose: Show list of all events
    @GetMapping
    public String list(Model model) {

        // Add all events to the model (send to view)
        model.addAttribute("events", repo.findAll());

        // Add all venues (used for displaying venue names)
        model.addAttribute("venues", venueRepo.findAll());

        // Return events list page
        return "events/list";
    }

    // Handles GET request to /events/new
    // Purpose: Show form to create new event
    @GetMapping("/new")
    public String createForm(Model model) {

        // Send empty event object to form
        model.addAttribute("event", new Event());

        // Send venues for dropdown selection
        model.addAttribute("venues", venueRepo.findAll());

        // Return event form page
        return "events/form";
    }

    // Handles GET request to /events/{id}/edit
    // Purpose: Show form to edit existing event
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {

        // Find event by ID, if not found give empty object
        model.addAttribute("event", repo.findById(id).orElse(new Event()));

        // Send venue list
        model.addAttribute("venues", venueRepo.findAll());

        // Use same form page for editing
        return "events/form";
    }

    // Handles POST request to /events
    // Purpose: Save new or updated event
    @PostMapping
    public String save(@ModelAttribute Event event, Model model) {

        // Check if event is new (no ID means new event)
        boolean isNew = event.getId() == null || event.getId().isBlank();

        // Validate date ONLY for new events
        if (isNew && event.getDate() != null && !event.getDate().isBlank()) {
            try {
                // Convert string date to LocalDate object
                LocalDate eventDate = LocalDate.parse(event.getDate());

                // Check if user selected a past date
                if (eventDate.isBefore(LocalDate.now())) {

                    // Add error message to model
                    model.addAttribute("dateError",
                            "Event date cannot be in the past. Please select today or a future date.");

                    // Reload venue list for form
                    model.addAttribute("venues", venueRepo.findAll());

                    // Stay on form page without saving
                    return "events/form";
                }

            } catch (Exception ignored) {
                // Ignore invalid date format errors
            }
        }

        // Save event using repository
        repo.save(event);

        // Redirect to event list page
        return "redirect:/events";
    }

    // Handles POST request to /events/{id}/delete
    // Purpose: Delete event
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {

        // Delete event by ID
        repo.deleteById(id);

        // Redirect to event list page
        return "redirect:/events";
    }
}