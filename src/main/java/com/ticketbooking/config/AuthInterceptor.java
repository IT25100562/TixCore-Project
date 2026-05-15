package com.ticketbooking.config;

import com.ticketbooking.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String path = req.getRequestURI();
        // Public paths
        if (path.equals("/login") || path.equals("/register") || path.equals("/logout")
                || path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")
                || path.equals("/favicon.ico")) {
            return true;
        }

        if (!SessionUtil.isLoggedIn(req.getSession(false))) {
            res.sendRedirect(req.getContextPath() + "/login");
            return false;
        }

        boolean isAdmin = SessionUtil.isAdmin(req.getSession());

        // User area: /app/**
        if (path.startsWith("/app/") || path.equals("/app")) {
            return true; // any logged-in user
        }

        // Everything else (admin CRUD + dashboard) requires admin
        if (!isAdmin) {
            res.sendRedirect(req.getContextPath() + "/app/home");
            return false;
        }
        return true;
    }
}
