package com.ticketbooking.util;

import jakarta.servlet.http.HttpSession;

public class SessionUtil {
    public static final String USER_ID    = "userId";
    public static final String USERNAME   = "username";
    public static final String FULL_NAME  = "fullName";
    public static final String ROLE       = "role";      // "admin" or "user"
    public static final String ADMIN_ROLE = "adminRole"; // "super_admin", "manager", "support"

    public static boolean isLoggedIn(HttpSession s) {
        return s != null && s.getAttribute(USER_ID) != null;
    }

    public static boolean isAdmin(HttpSession s) {
        return s != null && "admin".equals(s.getAttribute(ROLE));
    }

    public static boolean isUser(HttpSession s) {
        return s != null && "user".equals(s.getAttribute(ROLE));
    }

    /** Normalize: strip underscores, lowercase. "super_admin" → "superadmin" */
    private static String normalizeRole(HttpSession s) {
        if (!isAdmin(s)) return "";
        String ar = (String) s.getAttribute(ADMIN_ROLE);
        return ar == null ? "" : ar.replace("_", "").trim().toLowerCase();
    }

    /** True only for the super_admin / superadmin sub-role. */
    public static boolean isSuperAdmin(HttpSession s) {
        return "superadmin".equals(normalizeRole(s));
    }

    /** True for the manager sub-role. */
    public static boolean isManager(HttpSession s) {
        return "manager".equals(normalizeRole(s));
    }

    /** True for the support sub-role (view-only). */
    public static boolean isSupport(HttpSession s) {
        return "support".equals(normalizeRole(s));
    }

    /** Returns the raw admin sub-role string, or empty string if not an admin. */
    public static String getAdminRole(HttpSession s) {
        if (!isAdmin(s)) return "";
        String ar = (String) s.getAttribute(ADMIN_ROLE);
        return ar == null ? "" : ar;
    }

    /** Login for regular users (no adminRole). */
    public static void login(HttpSession s, String id, String username, String fullName, String role) {
        s.setAttribute(USER_ID, id);
        s.setAttribute(USERNAME, username);
        s.setAttribute(FULL_NAME, fullName);
        s.setAttribute(ROLE, role);
        s.removeAttribute(ADMIN_ROLE);
    }

    /** Login for admin accounts — stores the admin sub-role (super_admin / manager / support). */
    public static void loginAdmin(HttpSession s, String id, String username, String fullName, String adminRole) {
        s.setAttribute(USER_ID, id);
        s.setAttribute(USERNAME, username);
        s.setAttribute(FULL_NAME, fullName);
        s.setAttribute(ROLE, "admin");
        s.setAttribute(ADMIN_ROLE, adminRole == null ? "" : adminRole.trim().toLowerCase());
    }

    public static void logout(HttpSession s) {
        s.invalidate();
    }
}
