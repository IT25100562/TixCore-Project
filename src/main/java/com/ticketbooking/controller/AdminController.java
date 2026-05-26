package com.ticketbooking.controller;

import com.ticketbooking.model.Admin; // Import Admin model class
import com.ticketbooking.repository.AdminRepository; // Import Admin repository for database operations
// Spring framework imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller // Marks this class as a Spring MVC controller
@RequestMapping("/admins") // Base URL for all methods in this controller
public class AdminController {
    // Automatically inject AdminRepository object
    @Autowired private AdminRepository repo;
    // Display all admins
    @GetMapping
    public String list(Model model) {
        // Fetch all admins from database and send to view
        model.addAttribute("admins", repo.findAll());
        // Open admins/list.html
        return "admins/list";
    }
    // Show form to create a new admin
    @GetMapping("/new")
    public String createForm(Model model) {
        // Send empty Admin object to form
        model.addAttribute("admin", new Admin());
        // Open admins/form.html
        return "admins/form";
    }
    // Show form to edit an existing admin
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        // Find admin by ID, If not found, create empty Admin object 
        model.addAttribute("admin", repo.findById(id).orElse(new Admin()));
        // Open admins/form.html
        return "admins/form";
    }
    // Save admin data
    @PostMapping
    public String save(@ModelAttribute Admin admin,
                       // Get password from form
                       @RequestParam(name = "plainPassword", required = false) String plainPassword) {
        // Check whether this is a new admin
        boolean isNew = admin.getId() == null || admin.getId().isBlank();
        // If creating a new admin
        if (isNew) {
            admin.setPassword(plainPassword != null ? plainPassword.trim() : "");
        } else {
            // If editing existing admin

            // Check if new password was entered
            if (plainPassword != null && !plainPassword.isBlank()) {
                // Update password
                admin.setPassword(plainPassword.trim());
            } else {
                // If no new password entered,  keep old password
                repo.findById(admin.getId())
                    .ifPresent(existing -> admin.setPassword(existing.getPassword()));
            }
        }
        // Save admin into database
        repo.save(admin);
        // Redirect to admin list page
        return "redirect:/admins";
    }
    // Delete admin by ID
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
         // Delete admin from database
        repo.deleteById(id);
        // Redirect back to admin list
        return "redirect:/admins";
    }
}
