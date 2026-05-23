package com.ticketbooking.config;

import com.ticketbooking.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String path   = req.getRequestURI();
        String method = req.getMethod().toUpperCase();

        // ── Public paths (no auth needed) ────────────────────────────────────
        if (path.equals("/login") || path.equals("/register") || path.equals("/logout")
                || path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")
                || path.startsWith("/uploads/") || path.equals("/favicon.ico")) {
            return true;
        }

        HttpSession session = req.getSession(false);

        // ── Must be logged in ─────────────────────────────────────────────────
        if (!SessionUtil.isLoggedIn(session)) {
            res.sendRedirect(req.getContextPath() + "/login");
            return false;
        }

        // ── Regular user area: /app/** and /api/** ────────────────────────────
        if (path.startsWith("/app/") || path.equals("/app") || path.startsWith("/api/")) {
            return true;
        }

        // ── Admin area: must be an admin account ─────────────────────────────
        if (!SessionUtil.isAdmin(session)) {
            res.sendRedirect(req.getContextPath() + "/app/home");
            return false;
        }

        // ── Role-based access control within admin area ───────────────────────

        // SUPPORT → view-only: block any write operation or write-UI path
        if (SessionUtil.isSupport(session)) {
            boolean isWriteMethod = method.equals("POST") || method.equals("PUT")
                    || method.equals("DELETE") || method.equals("PATCH");
            boolean isWritePath   = path.endsWith("/new") || path.endsWith("/edit")
                    || path.endsWith("/delete");
            if (isWriteMethod || isWritePath) {
                res.sendRedirect(req.getContextPath() + "/?accessDenied=true");
                return false;
            }
            return true;
        }

        // MANAGER → can manage events, venues, bookings, reviews, seats, tickets
        //           but CANNOT access admin management (/admins) or user management (/users)
        if (SessionUtil.isManager(session)) {
            boolean restrictedPath = path.startsWith("/admins") || path.startsWith("/users");
            if (restrictedPath) {
                res.sendRedirect(req.getContextPath() + "/?accessDenied=true");
                return false;
            }
            return true;
        }

        // SUPER_ADMIN → full access, no restrictions
        return true;
    }
}
