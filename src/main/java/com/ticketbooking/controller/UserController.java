package com.ticketbooking.controller;

import com.ticketbooking.model.User;
import com.ticketbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// Marks this class as a Spring MVC controller
@Controller

// Base URL mapping for all user-related requests
@RequestMapping("/users")

public class UserController {

    // Used to perform CRUD operations on users
    @Autowired private UserRepository repo;

    // Handles GET requests to /users
    @GetMapping
    public String list(Model model) {

        // Adds all users to the model object
        model.addAttribute("users", repo.findAll());

        // Returns the users/list view page
        return "users/list";
    }

    // Opens the form to create a new user
    @GetMapping("/new")
    public String createForm(Model model) {

        // Adds an empty User object to the model
        model.addAttribute("user", new User());

        // Returns the user form page
        return "users/form";
    }

    // Opens the edit form for a specific user
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {

        // Finds the user by ID
        model.addAttribute("user", repo.findById(id).orElse(new User()));
        return "users/form";
    }

    // Saves a new user or updates an existing user
    @PostMapping
    public String save(@ModelAttribute User user) {
        repo.save(user);
        return "redirect:/users";
    }

    // Deletes a user by ID
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        repo.deleteById(id);
        return "redirect:/users";
    }
}

