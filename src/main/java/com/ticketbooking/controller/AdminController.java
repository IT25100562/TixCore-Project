package com.ticketbooking.controller;

import com.ticketbooking.model.Admin;
import com.ticketbooking.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admins")
public class AdminController {
    @Autowired private AdminRepository repo;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("admins", repo.findAll());
        return "admins/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("admin", new Admin());
        return "admins/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        model.addAttribute("admin", repo.findById(id).orElse(new Admin()));
        return "admins/form";
    }

    @PostMapping
    public String save(@ModelAttribute Admin admin) {
        repo.save(admin);
        return "redirect:/admins";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        repo.deleteById(id);
        return "redirect:/admins";
    }
}
