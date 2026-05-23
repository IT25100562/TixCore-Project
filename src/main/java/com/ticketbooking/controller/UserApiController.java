package com.ticketbooking.controller;

import com.ticketbooking.model.Admin;
import com.ticketbooking.model.User;
import com.ticketbooking.repository.AdminRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

// Marks this class as a REST API controller
@RestController

// Base URL mapping for all user-related API requests
@RequestMapping("/api/users")

public class UserApiController {

    // Validation rule for full names and phone numbers
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]{3,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    // Automatically injects the UserRepository object
    @Autowired private UserRepository userRepo;
    // Automatically injects the AdminRepository object
    @Autowired private AdminRepository adminRepo;

    // Handles GET requests to retrieve the current user's profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session) {

        // Checks whether the user is logged in
        if (!SessionUtil.isLoggedIn(session)) return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));

        // Retrieves the logged-in user's ID from the session
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        Map<String, Object> body = new LinkedHashMap<>();

        // Checks whether the logged-in user is an admin
        if (SessionUtil.isAdmin(session)) {
            Admin a = adminRepo.findById(userId).orElse(null);

            // Returns error if admin does not exist
            if (a == null) return ResponseEntity.status(404).body(Map.of("error", "Not found"));

            // Adds admin details to the response body
            body.put("id", a.getId());
            body.put("username", a.getUsername());
            body.put("fullName", a.getFullName());
            body.put("email", a.getEmail());
            body.put("phone", "");
            body.put("role", a.getRole());
            body.put("isAdmin", true);
        } else {

            // Finds the normal user by ID
            User u = userRepo.findById(userId).orElse(null);
            if (u == null) return ResponseEntity.status(404).body(Map.of("error", "Not found"));

            // Adds user details to the response body
            body.put("id", u.getId());
            body.put("username", u.getUsername());
            body.put("fullName", u.getFullName());
            body.put("email", u.getEmail());
            body.put("phone", u.getPhone());
            // Adds profile image with null safety check
            body.put("profileImage", u.getProfileImage() == null ? "" : u.getProfileImage());
            body.put("role", "user");
            body.put("isAdmin", false);
        }

        // Returns profile data as JSON response
        return ResponseEntity.ok(body);
    }

    // Handles PUT requests to update profile information
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));

        // Retrieves updated profile values from request body
        String fullName = body.getOrDefault("fullName", "").trim();
        String phone = body.getOrDefault("phone", "").trim();

        // Validates full name format
        if (!NAME_PATTERN.matcher(fullName).matches()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name must be at least 3 characters and contain letters/spaces only."));
        }
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);

        // Checks whether the logged-in user is an admin
        if (SessionUtil.isAdmin(session)) {
            Admin a = adminRepo.findById(userId).orElse(null);

            // Returns error if admin does not exist
            if (a == null) return ResponseEntity.status(404).body(Map.of("error", "Not found"));

            // Updates admin full name
            a.setFullName(fullName);
            adminRepo.save(a);
        } else {

            // Validates phone number format
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Phone must be exactly 10 digits."));
            }

            User u = userRepo.findById(userId).orElse(null);
            if (u == null) return ResponseEntity.status(404).body(Map.of("error", "Not found"));

            // Updates user profile details
            u.setFullName(fullName);
            u.setPhone(phone);
            userRepo.save(u);
        }

        // Refresh session display name
        session.setAttribute(SessionUtil.FULL_NAME, fullName);
        return ResponseEntity.ok(Map.of("success", true, "fullName", fullName, "phone", phone));
    }

    // Handles PUT requests to update passwords
    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> body, HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));

        // Retrieves password values from request body
        String current = body.getOrDefault("currentPassword", "");
        String next = body.getOrDefault("newPassword", "");
        String confirm = body.getOrDefault("confirmPassword", "");

        // Validates minimum password length
        if (next.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "New password must be at least 6 characters."));
        }

        // Checks whether new password and confirmation match
        if (!next.equals(confirm)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Passwords do not match."));
        }

        String userId = (String) session.getAttribute(SessionUtil.USER_ID);

        if (SessionUtil.isAdmin(session)) {
            Admin a = adminRepo.findById(userId).orElse(null);
            if (a == null) return ResponseEntity.status(404).body(Map.of("error", "Not found"));

            // Verifies current password
            if (!current.equals(a.getPassword())) {
                return ResponseEntity.status(403).body(Map.of("error", "Current password is incorrect."));
            }

            // Updates admin password
            a.setPassword(next);
            adminRepo.save(a);
        } else {

            User u = userRepo.findById(userId).orElse(null);
            if (u == null) return ResponseEntity.status(404).body(Map.of("error", "Not found"));

            // Verifies current password
            if (!current.equals(u.getPassword())) {
                return ResponseEntity.status(403).body(Map.of("error", "Current password is incorrect."));
            }

            // Updates user password
            u.setPassword(next);
            userRepo.save(u);
        }

        // Returns success response
        return ResponseEntity.ok(Map.of("success", true));
    }
}
