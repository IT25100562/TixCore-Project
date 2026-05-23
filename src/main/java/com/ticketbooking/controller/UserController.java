package com.ticketbooking.controller;

import com.ticketbooking.model.User;
import com.ticketbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {
    @Autowired private UserRepository repo;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", repo.findAll());
        return "users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        return "users/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        model.addAttribute("user", repo.findById(id).orElse(new User()));
        return "users/form";
    }

    @PostMapping
    public String save(@ModelAttribute User user) {
        repo.save(user);
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        repo.deleteById(id);
        return "redirect:/users";
    }
}
