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
import org.springframework.web.bind.annotation.GetMapping;

// Marks this class as a Spring MVC controller
@Controller
public class ProfileController {

    // Automatically injects the UserRepository object
    @Autowired private UserRepository userRepo;

    // Automatically injects the AdminRepository object
    @Autowired private AdminRepository adminRepo;

    // Displays the profile page of the currently logged-in user
    @GetMapping("/app/profile")
    public String profile(HttpSession session, Model model) {

        // Retrieves the logged-in user's ID from the session
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);

        // Checks whether the logged-in user is an admin
        if (SessionUtil.isAdmin(session)) {

            // Finds the admin by ID
            Admin a = adminRepo.findById(userId).orElse(null);

            // If admin does not exist, logout the session
            if (a == null) return "redirect:/logout";

            // Adds admin details to the model
            model.addAttribute("fullName", a.getFullName());
            model.addAttribute("username", a.getUsername());
            model.addAttribute("email", a.getEmail());
            model.addAttribute("phone", "");
            model.addAttribute("profileImage", "");
            model.addAttribute("role", a.getRole());
            model.addAttribute("isAdmin", true);
        } else {

            // Finds the normal user by ID
            User u = userRepo.findById(userId).orElse(null);

            // If user does not exist, logout the session
            if (u == null) return "redirect:/logout";

            // Adds user details to the model
            model.addAttribute("fullName", u.getFullName());
            model.addAttribute("username", u.getUsername());
            model.addAttribute("email", u.getEmail());
            model.addAttribute("phone", u.getPhone() == null ? "" : u.getPhone());
            model.addAttribute("profileImage", u.getProfileImage() == null ? "" : u.getProfileImage());
            model.addAttribute("role", "user");
            model.addAttribute("isAdmin", false);
        }

        // Returns the profile page view
        return "app/profile";
    }
}
