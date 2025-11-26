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
        int currentUserId = SessionUtil.getCurrentUserId(session);

        List<Project> projects = projectService.getProjectsByOwnerId(currentUserId);
        List<Project> assignedProjects = projectService.getAssignedProjectsByUserId(currentUserId);

        model.addAttribute("projects", projects);
        model.addAttribute("assignedProjects", assignedProjects);

        return "projects";
    }

    @GetMapping("/{projectId}")
    public String showProject(@PathVariable("projectId") int projectId, HttpSession session, Model model) {
        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        int currentUserId = SessionUtil.getCurrentUserId(session);

        // Check if the user has access to the project
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            return "redirect:/projects";
        }

        Project project = projectService.getProjectWithTree(projectId);
        String userRole = projectService.getUserRole(projectId, currentUserId);

        model.addAttribute("project", project);
        model.addAttribute("userRole", userRole);

        return "project";
    }

    @GetMapping("/create")
    public String showCreateProjectForm(HttpSession session, Model model){
        //If user is not logged in, show login screen
        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        Project newProject = new Project();
        newProject.setOwnerId(SessionUtil.getCurrentUserId(session));

        model.addAttribute("newProject", newProject);
        return "project_registration_form";
    }

    @PostMapping("/create")
    public String createProject(HttpSession session,
                                @Valid @ModelAttribute Project newProject,
                                BindingResult bindingResult,
                                Model model) {

        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        boolean fieldsHaveErrors = bindingResult.hasErrors();

        //if validation failed, return to form
        if (fieldsHaveErrors) {
            model.addAttribute("newProject", newProject);
            return "project_registration_form";
        }

        int projectId = projectService.createProject(newProject);

        return "redirect:/projects/" + projectId;
    }

    @GetMapping("/{parentId}/create")
    public String showCreateSubProjectForm(@PathVariable("parentId") int parentId, HttpSession session, Model model){
        //If user is not logged in, show login screen
        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        Project subProject = new Project();
        subProject.setOwnerId(projectService.getProject(parentId).getOwnerId());
        subProject.setParentProjectId(parentId);

        model.addAttribute("newProject", subProject);
        return "project_registration_form";
    }
}
