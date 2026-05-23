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

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]{3,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    @Autowired private UserRepository userRepo;
    @Autowired private AdminRepository adminRepo;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        Map<String, Object> body = new LinkedHashMap<>();
        if (SessionUtil.isAdmin(session)) {
            Admin a = adminRepo.findById(userId).orElse(null);
            if (a == null) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
            body.put("id", a.getId());
            body.put("username", a.getUsername());
            body.put("fullName", a.getFullName());
            body.put("email", a.getEmail());
            body.put("phone", "");
            body.put("role", a.getRole());
            body.put("isAdmin", true);
        } else {
            User u = userRepo.findById(userId).orElse(null);
            if (u == null) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
            body.put("id", u.getId());
            body.put("username", u.getUsername());
            body.put("fullName", u.getFullName());
            body.put("email", u.getEmail());
            body.put("phone", u.getPhone());
            body.put("profileImage", u.getProfileImage() == null ? "" : u.getProfileImage());
            body.put("role", "user");
            body.put("isAdmin", false);
        }
        return ResponseEntity.ok(body);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        String fullName = body.getOrDefault("fullName", "").trim();
        String phone = body.getOrDefault("phone", "").trim();

        if (!NAME_PATTERN.matcher(fullName).matches()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name must be at least 3 characters and contain letters/spaces only."));
        }
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        if (SessionUtil.isAdmin(session)) {
            Admin a = adminRepo.findById(userId).orElse(null);
            if (a == null) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
            a.setFullName(fullName);
            adminRepo.save(a);
        } else {
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Phone must be exactly 10 digits."));
            }
            User u = userRepo.findById(userId).orElse(null);
            if (u == null) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
            u.setFullName(fullName);
            u.setPhone(phone);
            userRepo.save(u);
        }
        // Refresh session display name
        session.setAttribute(SessionUtil.FULL_NAME, fullName);
        return ResponseEntity.ok(Map.of("success", true, "fullName", fullName, "phone", phone));
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> body, HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        String current = body.getOrDefault("currentPassword", "");
        String next = body.getOrDefault("newPassword", "");
        String confirm = body.getOrDefault("confirmPassword", "");

        if (next.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "New password must be at least 6 characters."));
        }
        if (!next.equals(confirm)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Passwords do not match."));
        }

        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        if (SessionUtil.isAdmin(session)) {
            Admin a = adminRepo.findById(userId).orElse(null);
            if (a == null) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
            if (!current.equals(a.getPassword())) {
                return ResponseEntity.status(403).body(Map.of("error", "Current password is incorrect."));
            }
            a.setPassword(next);
            adminRepo.save(a);
        } else {
            User u = userRepo.findById(userId).orElse(null);
            if (u == null) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
            if (!current.equals(u.getPassword())) {
                return ResponseEntity.status(403).body(Map.of("error", "Current password is incorrect."));
            }
            u.setPassword(next);
            userRepo.save(u);
        }
        return ResponseEntity.ok(Map.of("success", true));
    }
}
