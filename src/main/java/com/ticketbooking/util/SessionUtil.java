package com.ticketbooking.util;

import jakarta.servlet.http.HttpSession;

public class SessionUtil {
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String FULL_NAME = "fullName";
    public static final String ROLE = "role"; // "admin" or "user"

    public static boolean isLoggedIn(HttpSession s) {
        return s != null && s.getAttribute(USER_ID) != null;
    }

    public static boolean isAdmin(HttpSession s) {
        return s != null && "admin".equals(s.getAttribute(ROLE));
    }

    public static boolean isUser(HttpSession s) {
        return s != null && "user".equals(s.getAttribute(ROLE));
    }

    public static void login(HttpSession s, String id, String username, String fullName, String role) {
        s.setAttribute(USER_ID, id);
        s.setAttribute(USERNAME, username);
        s.setAttribute(FULL_NAME, fullName);
        s.setAttribute(ROLE, role);
    }

    public static void logout(HttpSession s) {
        s.invalidate();
    }
}
