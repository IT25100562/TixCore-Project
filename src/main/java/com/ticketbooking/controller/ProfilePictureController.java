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

// Marks this class as a REST API controller
@RestController

// Base URL mapping for all user-related API requests
@RequestMapping("/api/users")
public class ProfilePictureController {

    // Reads the upload directory path from application.properties
    @Value("${app.uploads.dir}")
    private String uploadsDir;

    // Automatically injects the UserRepository object
    @Autowired
    private UserRepository userRepo;

    // Handles POST requests to upload a profile picture
    @PostMapping("/profile-picture")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            HttpSession session
    ) throws IOException {

        // Checks whether the user is logged in
        if (!SessionUtil.isLoggedIn(session)) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Not logged in"));
        }

        // Prevents admins from uploading profile images
        if (SessionUtil.isAdmin(session)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Admins do not have profile images."));
        }

        // Checks whether a file was uploaded
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No file uploaded."));
        }

        // Limits file size to 2MB
        if (file.getSize() > 2 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File must be 2MB or less."));
        }

        // Gets the uploaded file content type
        String contentType = file.getContentType();
        String ext;

        // Allows JPG/JPEG image files
        if ("image/jpeg".equalsIgnoreCase(contentType)
                || "image/jpg".equalsIgnoreCase(contentType)) {

            ext = "jpg";

        }
        // Allows PNG image files
        else if ("image/png".equalsIgnoreCase(contentType)) {

            ext = "png";

        }
        // Rejects unsupported file types
        else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only JPG or PNG images allowed."));
        }

        // Retrieves the logged-in user's ID from the session
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);

        // Creates the uploads/profiles directory if it does not exist
        Path dir = Paths.get(uploadsDir, "profiles");
        Files.createDirectories(dir);

        // Generates a unique filename for the uploaded image
        String filename = userId + "-"
                + UUID.randomUUID().toString().substring(0, 8)
                + "." + ext;

        // Creates the target file path
        Path target = dir.resolve(filename);

        // Copies the uploaded file into the target directory
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Finds the current user
        User u = userRepo.findById(userId).orElse(null);

        // Returns error if user does not exist
        if (u == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "User not found"));
        }

        // Deletes the previous profile image if it exists
        if (u.getProfileImage() != null && !u.getProfileImage().isBlank()) {
            try {
                Files.deleteIfExists(
                        Paths.get(uploadsDir).resolve(
                                u.getProfileImage().replaceFirst("^/uploads/", "")
                        )
                );
            }
            // Ignores file deletion errors
            catch (Exception ignored) {
            }
        }

        // Creates the public image URL
        String url = "/uploads/profiles/" + filename;

        // Saves the new profile image URL
        u.setProfileImage(url);
        userRepo.save(u);

        // Returns success response with image URL
        return ResponseEntity.ok(Map.of("url", url));
    }

    // Handles DELETE requests to remove a profile picture
    @DeleteMapping("/profile-picture")
    public ResponseEntity<?> remove(HttpSession session) {

        // Checks whether the user is authorized
        if (!SessionUtil.isLoggedIn(session) || SessionUtil.isAdmin(session)) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Not authorized"));
        }

        String userId = (String) session.getAttribute(SessionUtil.USER_ID);

        // Finds the current user
        User u = userRepo.findById(userId).orElse(null);

        // Returns error if user does not exist
        if (u == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Not found"));
        }

        // Deletes the existing profile image file
        if (u.getProfileImage() != null && !u.getProfileImage().isBlank()) {
            try {
                Files.deleteIfExists(
                        Paths.get(uploadsDir).resolve(
                                u.getProfileImage().replaceFirst("^/uploads/", "")
                        )
                );
            }
            // Ignores file deletion errors
            catch (Exception ignored) {
            }
        }

        // Removes the profile image URL from the user
        u.setProfileImage("");
        userRepo.save(u);

        // Returns success response
        return ResponseEntity.ok(Map.of("success", true));
    }
}