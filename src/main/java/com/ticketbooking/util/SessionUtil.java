package com.ticketbooking.util;

import jakarta.servlet.http.HttpSession;

// Utility class used for managing user session information
public class SessionUtil {

    // Session attribute keys
    public static final String USER_ID    = "userId";
    public static final String USERNAME   = "username";
    public static final String FULL_NAME  = "fullName";

    // Stores whether the logged-in account is admin or user
    public static final String ROLE       = "role";      // "admin" or "user"
    public static final String ADMIN_ROLE = "adminRole"; // "super_admin", "manager", "support"

    // Checks whether a user is currently logged in
    public static boolean isLoggedIn(HttpSession s) {
        return s != null && s.getAttribute(USER_ID) != null;
    }

    // Checks whether the logged-in account is an admin
    public static boolean isAdmin(HttpSession s) {
        return s != null && "admin".equals(s.getAttribute(ROLE));
    }

    // Checks whether the logged-in account is a normal user
    public static boolean isUser(HttpSession s) {
        return s != null && "user".equals(s.getAttribute(ROLE));
    }

    // Removes underscores and converts text to lowercase
    // Example: "super_admin" -> "superadmin"
    private static String normalizeRole(HttpSession s) {

        // Returns empty string if not admin
        if (!isAdmin(s)) {
            return "";
        }

        // Retrieves admin role from session
        String ar = (String) s.getAttribute(ADMIN_ROLE);

        // Returns normalized role name
        return ar == null
                ? ""
                : ar.replace("_", "").trim().toLowerCase();
    }

    // Checks whether the logged-in admin is a super admin
    public static boolean isSuperAdmin(HttpSession s) {
        return "superadmin".equals(normalizeRole(s));
    }

    // Checks whether the logged-in admin is a manager
    public static boolean isManager(HttpSession s) {
        return "manager".equals(normalizeRole(s));
    }

    // Checks whether the logged-in admin is support staff
    public static boolean isSupport(HttpSession s) {
        return "support".equals(normalizeRole(s));
    }

    // Returns the raw admin role value
    public static String getAdminRole(HttpSession s) {

        if (!isAdmin(s)) {
            return "";
        }

        String ar = (String) s.getAttribute(ADMIN_ROLE);

        return ar == null ? "" : ar;
    }

    // Stores login session data for normal users
    public static void login(
            HttpSession s,
            String id,
            String username,
            String fullName,
            String role
    ) {

        // Stores user session attributes
        s.setAttribute(USER_ID, id);
        s.setAttribute(USERNAME, username);
        s.setAttribute(FULL_NAME, fullName);
        s.setAttribute(ROLE, role);

        // Removes admin role for normal users
        s.removeAttribute(ADMIN_ROLE);
    }

    // Stores login session data for admin accounts
    public static void loginAdmin(
            HttpSession s,
            String id,
            String username,
            String fullName,
            String adminRole
    ) {

        // Stores admin session attributes
        s.setAttribute(USER_ID, id);
        s.setAttribute(USERNAME, username);
        s.setAttribute(FULL_NAME, fullName);
        s.setAttribute(ROLE, "admin");

        // Stores normalized admin sub-role
        s.setAttribute(
                ADMIN_ROLE,
                adminRole == null
                        ? ""
                        : adminRole.trim().toLowerCase()
        );
    }

    // Clears the current session and logs out the user
    public static void logout(HttpSession s) {
        s.invalidate();
    }
}