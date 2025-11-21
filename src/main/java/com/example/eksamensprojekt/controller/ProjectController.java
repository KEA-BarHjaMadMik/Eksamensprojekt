package com.example.eksamensprojekt.controller;

import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.service.ProjectService;
import com.example.eksamensprojekt.utils.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("projects")
public class ProjectController {
    private final ProjectService service;

    public ProjectController(ProjectService service){
        this.service = service;
    }

    @GetMapping("/project/{projectID}")
    public String showProject(@PathVariable("projectID") int projectID, HttpSession session, Model model){
        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        Project project = service.getProject(projectID);
        model.addAttribute("project", project);
        return "project";
    }
}
