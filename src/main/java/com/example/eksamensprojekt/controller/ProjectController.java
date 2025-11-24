package com.example.eksamensprojekt.controller;

import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.service.ProjectService;
import com.example.eksamensprojekt.utils.SessionUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService){
        this.projectService = projectService;
    }

    @GetMapping("/{projectID}")
    public String showProject(@PathVariable("projectID") int projectID, HttpSession session, Model model) {
        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        Project project = projectService.getProject(projectID);
        model.addAttribute("project", project);
        return "project";
    }

    @GetMapping("/create")
    public String createProject(HttpSession session,
                                @Valid @ModelAttribute Project newProject,
                                BindingResult bindingResult,
                                Model model) {

        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        boolean fieldsHaveErrors = bindingResult.hasErrors();
        newProject.setOwnerID(setProjectOwner(session));

        //if validation failed, return to form
        if (fieldsHaveErrors){
            model.addAttribute("project", newProject);
            return "project_registration_form";
        }

        projectService.createProject(newProject);
        int parentID = newProject.getParentProjectId();

        if (parentID != 0){
            return "redirect:/" + parentID + "/" + newProject.getProjectId();
        } else {
            return "redirect:/" + newProject.getProjectId();
        }
    }

    //Helper methods
    private int setProjectOwner(HttpSession session){
        return (int) session.getAttribute("userID");
    }
}
