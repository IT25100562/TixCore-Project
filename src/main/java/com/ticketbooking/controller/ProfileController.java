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

@Controller
public class ProfileController {

    @Autowired private UserRepository userRepo;
    @Autowired private AdminRepository adminRepo;

    @GetMapping("/app/profile")
    public String profile(HttpSession session, Model model) {
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        if (SessionUtil.isAdmin(session)) {
            Admin a = adminRepo.findById(userId).orElse(null);
            if (a == null) return "redirect:/logout";
            model.addAttribute("fullName", a.getFullName());
            model.addAttribute("username", a.getUsername());
            model.addAttribute("email", a.getEmail());
            model.addAttribute("phone", "");
            model.addAttribute("profileImage", "");
            model.addAttribute("role", a.getRole());
            model.addAttribute("isAdmin", true);
        } else {
            User u = userRepo.findById(userId).orElse(null);
            if (u == null) return "redirect:/logout";
            model.addAttribute("fullName", u.getFullName());
            model.addAttribute("username", u.getUsername());
            model.addAttribute("email", u.getEmail());
            model.addAttribute("phone", u.getPhone() == null ? "" : u.getPhone());
            model.addAttribute("profileImage", u.getProfileImage() == null ? "" : u.getProfileImage());
            model.addAttribute("role", "user");
            model.addAttribute("isAdmin", false);
        }
        return "app/profile";
    }
}
