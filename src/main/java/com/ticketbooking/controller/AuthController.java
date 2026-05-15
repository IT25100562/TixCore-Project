package com.ticketbooking.controller;

import com.ticketbooking.model.Admin;
import com.ticketbooking.model.User;
import com.ticketbooking.repository.AdminRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired private UserRepository userRepo;
    @Autowired private AdminRepository adminRepo;

    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        if (SessionUtil.isLoggedIn(session)) {
            return SessionUtil.isAdmin(session) ? "redirect:/" : "redirect:/app/home";
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        // Check admins first
        Optional<Admin> admin = adminRepo.findAll().stream()
            .filter(a -> a.getUsername().equalsIgnoreCase(username) && password.equals(a.getPassword()))
            .findFirst();
        if (admin.isPresent()) {
            SessionUtil.login(session, admin.get().getId(), admin.get().getUsername(),
                              admin.get().getFullName(), "admin");
            return "redirect:/";
        }
        // Then users
        Optional<User> user = userRepo.findAll().stream()
            .filter(u -> u.getUsername().equalsIgnoreCase(username) && password.equals(u.getPassword()))
            .findFirst();
        if (user.isPresent()) {
            SessionUtil.login(session, user.get().getId(), user.get().getUsername(),
                              user.get().getFullName(), "user");
            return "redirect:/app/home";
        }
        model.addAttribute("error", "Invalid username or password.");
        model.addAttribute("username", username);
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model, HttpSession session) {
        if (SessionUtil.isLoggedIn(session)) {
            return SessionUtil.isAdmin(session) ? "redirect:/" : "redirect:/app/home";
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam(required = false) String phone,
                           HttpSession session,
                           Model model) {
        boolean exists = userRepo.findAll().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username))
            || adminRepo.findAll().stream().anyMatch(a -> a.getUsername().equalsIgnoreCase(username));
        if (exists) {
            model.addAttribute("error", "Username already taken.");
            model.addAttribute("username", username);
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            return "auth/register";
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPhone(phone == null ? "" : phone);
        userRepo.save(u);
        SessionUtil.login(session, u.getId(), u.getUsername(), u.getFullName(), "user");
        return "redirect:/app/home";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        SessionUtil.logout(session);
        return "redirect:/login";
    }
}
