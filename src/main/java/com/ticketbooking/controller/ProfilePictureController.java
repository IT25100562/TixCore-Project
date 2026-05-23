package com.ticketbooking.controller;

import com.ticketbooking.model.User;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class ProfilePictureController {

    @Value("${app.uploads.dir}")
    private String uploadsDir;

    @Autowired private UserRepository userRepo;

    @PostMapping("/profile-picture")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
        if (!SessionUtil.isLoggedIn(session)) return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        if (SessionUtil.isAdmin(session)) return ResponseEntity.status(403).body(Map.of("error", "Admins do not have profile images."));
        if (file == null || file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded."));
        if (file.getSize() > 2 * 1024 * 1024) return ResponseEntity.badRequest().body(Map.of("error", "File must be 2MB or less."));

        String contentType = file.getContentType();
        String ext;
        if ("image/jpeg".equalsIgnoreCase(contentType) || "image/jpg".equalsIgnoreCase(contentType)) ext = "jpg";
        else if ("image/png".equalsIgnoreCase(contentType)) ext = "png";
        else return ResponseEntity.badRequest().body(Map.of("error", "Only JPG or PNG images allowed."));

        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        Path dir = Paths.get(uploadsDir, "profiles");
        Files.createDirectories(dir);
        String filename = userId + "-" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
        Path target = dir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        User u = userRepo.findById(userId).orElse(null);
        if (u == null) return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        // Delete previous file
        if (u.getProfileImage() != null && !u.getProfileImage().isBlank()) {
            try { Files.deleteIfExists(Paths.get(uploadsDir).resolve(u.getProfileImage().replaceFirst("^/uploads/", ""))); }
            catch (Exception ignored) {}
        }
        String url = "/uploads/profiles/" + filename;
        u.setProfileImage(url);
        userRepo.save(u);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @DeleteMapping("/profile-picture")
    public ResponseEntity<?> remove(HttpSession session) {
        if (!SessionUtil.isLoggedIn(session) || SessionUtil.isAdmin(session))
            return ResponseEntity.status(401).body(Map.of("error", "Not authorized"));
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        User u = userRepo.findById(userId).orElse(null);
        if (u == null) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
        if (u.getProfileImage() != null && !u.getProfileImage().isBlank()) {
            try { Files.deleteIfExists(Paths.get(uploadsDir).resolve(u.getProfileImage().replaceFirst("^/uploads/", ""))); }
            catch (Exception ignored) {}
        }
        u.setProfileImage("");
        userRepo.save(u);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
