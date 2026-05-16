package com.ticketbooking.controller;

import com.ticketbooking.model.Review;
import com.ticketbooking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    @Autowired private ReviewRepository repo;
    @Autowired private UserRepository userRepo;
    @Autowired private EventRepository eventRepo;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("reviews", repo.findAll());
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("events", eventRepo.findAll());
        return "reviews/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        Review r = new Review();
        r.setRating(5);
        model.addAttribute("review", r);
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("events", eventRepo.findAll());
        return "reviews/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        model.addAttribute("review", repo.findById(id).orElse(new Review()));
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("events", eventRepo.findAll());
        return "reviews/form";
    }

    @PostMapping
    public String save(@ModelAttribute Review review) {
        repo.save(review);
        return "redirect:/reviews";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        repo.deleteById(id);
        return "redirect:/reviews";
    }
}
