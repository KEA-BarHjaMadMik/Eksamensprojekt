package com.example.eksamensprojekt.utils;

import jakarta.servlet.http.HttpSession;

public class SessionUtil {
    public static boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("userID") != null;
    }

    public static int getCurrentUserID(HttpSession session) {
        return (int) session.getAttribute("userID");
    }
}
