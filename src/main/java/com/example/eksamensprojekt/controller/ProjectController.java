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

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public String projects(HttpSession session, Model model) {
        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";
        int currentUserID = (int) session.getAttribute("userID");

        List<Project> projects = projectService.getProjectsByOwnerID(currentUserID);
        model.addAttribute("projects", projects);

        return "projects";
    }

    @GetMapping("/{projectID}")
    public String showProject(@PathVariable("projectID") int projectId, HttpSession session, Model model) {
        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        int currentUserID = (int) session.getAttribute("userID");

        // Check if the user has access to the project
        if (!projectService.hasAccessToProject(projectId, currentUserID)) {
            return "redirect:/projects";
        }

        Project project = projectService.getProject(projectId);
        String userRole = projectService.getUserRole(project, currentUserID);

        model.addAttribute("project", project);
        model.addAttribute("userRole", userRole);

        return "project";
    }

    @GetMapping("/create")
    public String showCreateProjectForm(HttpSession session, Model model){
        //If user is not logged in, show login screen
        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        model.addAttribute("newProject", new Project());
        return "project_registration_form";
    }

    @PostMapping("/create")
    public String createProject(HttpSession session,
                                @Valid @ModelAttribute Project newProject,
                                BindingResult bindingResult,
                                Model model) {

        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        boolean fieldsHaveErrors = bindingResult.hasErrors();
        newProject.setOwnerID(setProjectOwner(session));

        //if validation failed, return to form
        if (fieldsHaveErrors) {
            model.addAttribute("newProject", newProject);
            return "project_registration_form";
        }

        int projectID = projectService.createProject(newProject);

        return "redirect:/projects/" + projectID;
    }

    @GetMapping("/{parentID}/create")
    public String showCreateSubProjectForm(HttpSession session, Model model, @PathVariable("parentID") int parentID){
        //If user is not logged in, show login screen
        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        Project subProject = new Project();
        subProject.setParentProjectId(parentID);

        model.addAttribute("newProject", subProject);
        return "project_registration_form";
    }

    //Helper methods
    private int setProjectOwner(HttpSession session) {
        return (int) session.getAttribute("userID");
    }
}
