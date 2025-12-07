package com.example.eksamensprojekt.advice;

import com.example.eksamensprojekt.controller.ProjectController;
import com.example.eksamensprojekt.controller.TaskController;
import com.example.eksamensprojekt.exceptions.NotLoggedInException;
import com.example.eksamensprojekt.utils.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = { ProjectController.class, TaskController.class })
public class AuthenticationAdvice {

    @ModelAttribute
    public void checkLogin(HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) {
            throw new NotLoggedInException();
        }
    }
}
