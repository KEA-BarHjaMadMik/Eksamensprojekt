package com.example.eksamensprojekt.advice;

import com.example.eksamensprojekt.utils.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// provides global model attributes to all controllers
@ControllerAdvice
public class GlobalModelAttributes {

    // Adds 'isLoggedIn' and 'username' attributes to the model for all views
    @ModelAttribute
    public void addGlobalAttributes(HttpSession session, Model model) {
        boolean isLoggedIn = SessionUtil.isLoggedIn(session);
        model.addAttribute("isLoggedIn", isLoggedIn);

        if (isLoggedIn) {
            model.addAttribute("userId", session.getAttribute("userId"));
            model.addAttribute("userEmail", session.getAttribute("userEmail"));
        }
    }
}
