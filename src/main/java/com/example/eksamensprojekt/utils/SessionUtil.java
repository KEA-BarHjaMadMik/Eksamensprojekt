package com.example.eksamensprojekt.utils;

import jakarta.servlet.http.HttpSession;

public class SessionUtil {
    public static boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("username") != null;
    }
}
