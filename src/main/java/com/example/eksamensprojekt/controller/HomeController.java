package com.example.eksamensprojekt.controller;

import com.example.eksamensprojekt.utils.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showHomePage(HttpSession session) {
        return !SessionUtil.isLoggedIn(session) ? "index" : "redirect:/projects";
    }
}
