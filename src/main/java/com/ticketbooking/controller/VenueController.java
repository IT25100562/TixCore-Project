package com.ticketbooking.controller;

import com.ticketbooking.model.Venue;
import com.ticketbooking.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/venues")
public class VenueController {
    @Autowired private VenueRepository repo;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("venues", repo.findAll());
        return "venues/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("venue", new Venue());
        return "venues/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        model.addAttribute("venue", repo.findById(id).orElse(new Venue()));
        return "venues/form";
    }

    @PostMapping
    public String save(@ModelAttribute Venue venue) {
        repo.save(venue);
        return "redirect:/venues";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        repo.deleteById(id);
        return "redirect:/venues";
    }
}
