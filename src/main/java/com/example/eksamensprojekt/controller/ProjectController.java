package com.example.eksamensprojekt.controller;

import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.model.ProjectRole;
import com.example.eksamensprojekt.model.User;
import com.example.eksamensprojekt.service.ProjectService;
import com.example.eksamensprojekt.service.UserService;
import com.example.eksamensprojekt.utils.SessionUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("projects")
public class ProjectController {
    private final ProjectService projectService;
    private final UserService userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    // =========== PROJECT CRUD ===========

    @GetMapping
    public String projects(HttpSession session, Model model) {
        int currentUserId = SessionUtil.getCurrentUserId(session);

        List<Project> projects = projectService.getProjectsByOwnerId(currentUserId);
        List<Project> assignedProjects = projectService.getAssignedProjectsByUserId(currentUserId);

        model.addAttribute("projects", projects);
        model.addAttribute("assignedProjects", assignedProjects);

        return "projects";
    }

    @GetMapping("/{projectId}")
    public String showProject(@PathVariable("projectId") int projectId, HttpSession session, Model model) {
        int currentUserId = SessionUtil.getCurrentUserId(session);

        // Check if the user has access to the project
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            return "redirect:/projects";
        }

        Project project = projectService.getProjectWithTree(projectId);
        ProjectRole projectRole = projectService.getUserRole(projectId, currentUserId);
        String userRole = projectRole != null ? projectRole.getRole() : "OWNER";

        model.addAttribute("project", project);
        model.addAttribute("userRole", userRole);

        return "project";
    }

    @GetMapping("/{projectId}/hour_distribution")
    public String showHourDistribution(@PathVariable("projectId") int projectId,
                                       HttpSession session,
                                       Model model) {
        int currentUserId = SessionUtil.getCurrentUserId(session);

        // Check if the user has access to the project
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            return "redirect:/projects";
        }

        Project project = projectService.getProjectWithTree(projectId);
        Map<LocalDate, Double> hourDistributionMap = project.getDistributedHours();

        model.addAttribute("project", project);
        model.addAttribute("hourDistributionMap", hourDistributionMap);

        return "project_hour_distribution";
    }

    @GetMapping("/create")
    public String showCreateProjectForm(HttpSession session, Model model) {
        Project newProject = new Project();
        newProject.setOwnerId(SessionUtil.getCurrentUserId(session));
        newProject.setStartDate(LocalDate.now());
        newProject.setEndDate(LocalDate.now());

        model.addAttribute("newProject", newProject);
        return "project_registration_form";
    }

    @PostMapping("/create")
    public String createProject(@Valid @ModelAttribute Project newProject,
                                BindingResult bindingResult,
                                Model model) {
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
    public String showCreateSubProjectForm(@PathVariable("parentId") int parentId, Model model) {
        Project subProject = new Project();
        subProject.setOwnerId(projectService.getProject(parentId).getOwnerId());
        subProject.setParentProjectId(parentId);
        subProject.setStartDate(LocalDate.now());
        subProject.setEndDate(LocalDate.now());

        model.addAttribute("newProject", subProject);
        return "project_registration_form";
    }

    @GetMapping("/{projectId}/edit")
    public String showEditProjectForm(@PathVariable("projectId") int projectId,
                                      HttpSession session,
                                      Model model) {
        int currentUserId = SessionUtil.getCurrentUserId(session);
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            return "redirect:/projects";
        }

        Project project = projectService.getProject(projectId);
        ProjectRole userRole = projectService.getUserRole(projectId, currentUserId);

        boolean isOwner = project.getOwnerId() == currentUserId;
        boolean hasFullAccess = userRole != null && userRole.getRole().equals("FULL_ACCESS");

        // Only owner and full access can edit
        if (!isOwner && !hasFullAccess) {
            return "redirect:/projects/" + projectId;
        }

        model.addAttribute("project", project);
        model.addAttribute("userRole", userRole != null ? userRole.getRole() : "OWNER");
        return "project_edit_form";
    }

    @PostMapping("/{projectId}/edit")
    public String updateProject(@PathVariable("projectId") int projectId,
                                @Valid @ModelAttribute("project") Project project,
                                BindingResult bindingResult,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        int currentUserId = SessionUtil.getCurrentUserId(session);

        project.setProjectId(projectId);

        // Check access
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            return "redirect:/projects";
        }

        Project existingProject = projectService.getProject(projectId);
        ProjectRole userRole = projectService.getUserRole(projectId, currentUserId);

        // Only owner and full access can edit
        boolean isOwner = existingProject.getOwnerId() == currentUserId;
        boolean hasFullAccess = userRole != null && userRole.getRole().equals("FULL_ACCESS");

        if (!isOwner && !hasFullAccess) {
            return "redirect:/projects/" + projectId;
        }

        // Keep owner ID and parent project ID unchanged
        project.setOwnerId(existingProject.getOwnerId());
        project.setParentProjectId(existingProject.getParentProjectId());

        if (bindingResult.hasErrors()) {
            return "project_edit_form";
        }

        if (projectService.updateProject(project)) {
            redirectAttributes.addFlashAttribute("updateSuccess", true);
        }

        return "redirect:/projects/" + projectId;
    }

    @PostMapping("/{projectId}/delete")
    public String deleteProject(@PathVariable("projectId") int projectId, HttpSession session) {
        int currentUserId = SessionUtil.getCurrentUserId(session);
        // Check access
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            return "redirect:/projects";
        }

        // Only owner can delete
        ProjectRole userRole = projectService.getUserRole(projectId, currentUserId);
        if (userRole == null || !"OWNER".equals(userRole.getRole())) {
            return "redirect:/projects";
        }

        // check whether it's a subproject before deletion, for proper redirection
        Project project = projectService.getProject(projectId);
        if (project == null) {
            return "redirect:/projects";
        }
        Integer parentProjectId = project.getParentProjectId();

        // proceed with delete
        projectService.deleteProject(projectId);

        // redirect to parent, if subproject
        if (parentProjectId != null) {
            return "redirect:/projects/" + parentProjectId;
        }
        // else return to projects
        return "redirect:/projects";
    }

    // ===========TEAM MANAGEMENT===========

    @GetMapping("/{projectId}/team")
    public String showTeam(@PathVariable("projectId") int projectId,
                           HttpSession session,
                           Model model) {
        int currentUserId = SessionUtil.getCurrentUserId(session);
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            return "redirect:/projects";
        }

        Project project = projectService.getProject(projectId);

        Map<User, ProjectRole> projectUsersWithDirectRoles = projectService.getProjectUsersWithDirectRoles(projectId);
        Map<User, ProjectRole> projectUsersWithInheritedRoles = projectService.getProjectUsersWithInheritedRoles(projectId);

        List<ProjectRole> projectRoles = projectService.getAllProjectRoles();

        //Get all users for the datalist
        List<User> allUsers = userService.getAllUsers();

        model.addAttribute("project", project);
        model.addAttribute("directProjectUsers", projectUsersWithDirectRoles);
        model.addAttribute("inheritedProjectUsers", projectUsersWithInheritedRoles);
        model.addAttribute("projectRoles", projectRoles);
        model.addAttribute("allUsers", allUsers);
        return "project_team";
    }

    @PostMapping("/{projectId}/team/add")
    public String addTeamMember(@PathVariable("projectId") int projectId,
                                @RequestParam("email") String email,
                                @RequestParam("role") String role,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        int currentUserId = SessionUtil.getCurrentUserId(session);
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            return "redirect:/projects";
        }

        // if a user with the provided email exists, proceed
        if (!userService.emailExists(email)) {
            redirectAttributes.addFlashAttribute("addErrorMessage", "Bruger med e-mail, " + email + ", ikke fundet");
        } else if (projectService
                .getDirectProjectUsers(projectId)
                .stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email))) {
            redirectAttributes.addFlashAttribute("addErrorMessage", "Bruger med e-mail, " + email + ", er allerede tilknyttet projektet");
        } else {
            projectService.addUserToProject(projectId, email, role);
        }

        return String.format("redirect:/projects/%s/team", projectId);
    }

    @PostMapping("/{projectId}/team/{userId}/update_role")
    public String updateTeamMemberRole(@PathVariable("projectId") int projectId,
                                       @PathVariable("userId") int userId,
                                       @RequestParam("role") String role,
                                       HttpSession session) {
        int currentUserId = SessionUtil.getCurrentUserId(session);

        // Access check
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            return "redirect:/projects";
        }

        // Prevent updating the owner's role
        Project project = projectService.getProject(projectId);
        if (project.getOwnerId() == userId) {
            return String.format("redirect:/projects/%s/team", projectId);
        }

        // Update the role
        projectService.updateUserRole(projectId, userId, role);

        return String.format("redirect:/projects/%s/team", projectId);
    }

    @PostMapping("/{projectId}/team/{userId}/remove")
    public String removeTeamMember(@PathVariable("projectId") int projectId,
                                   @PathVariable("userId") int userId,
                                   HttpSession session) {
        int currentUserId = SessionUtil.getCurrentUserId(session);

        // Access check
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            return "redirect:/projects";
        }

        // Prevent removing the project owner
        Project project = projectService.getProject(projectId);
        if (project.getOwnerId() == userId) {
            return String.format("redirect:/projects/%s/team", projectId);
        }

        projectService.removeUserFromProject(projectId, userId);

        return String.format("redirect:/projects/%s/team", projectId);
    }
}
