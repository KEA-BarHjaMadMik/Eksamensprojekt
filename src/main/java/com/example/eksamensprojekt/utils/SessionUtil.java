package com.example.eksamensprojekt.utils;

import jakarta.servlet.http.HttpSession;

public class SessionUtil {
    public static boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("userId") != null;
    }

    public static int getCurrentUserId(HttpSession session) {
        return (int) session.getAttribute("userId");
    }
}
