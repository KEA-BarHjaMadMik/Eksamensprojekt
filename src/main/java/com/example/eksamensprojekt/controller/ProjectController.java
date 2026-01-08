package com.example.eksamensprojekt.controller;

import com.example.eksamensprojekt.exceptions.AccessDeniedException;
import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.model.ProjectDTO;
import com.example.eksamensprojekt.model.ProjectRole;
import com.example.eksamensprojekt.model.User;
import com.example.eksamensprojekt.service.HourDistributionService;
import com.example.eksamensprojekt.service.ProjectService;
import com.example.eksamensprojekt.service.UserService;
import com.example.eksamensprojekt.utils.SessionUtil;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@SuppressWarnings("JvmTaintAnalysis")
@Controller
@RequestMapping("projects")
public class ProjectController {
    private final ProjectService projectService;
    private final UserService userService;
    private final HourDistributionService hourDistributionService;

    public ProjectController(ProjectService projectService,
                             UserService userService,
                             HourDistributionService hourDistributionService) {
        this.projectService = projectService;
        this.userService = userService;
        this.hourDistributionService = hourDistributionService;
    }

    // =========== PROJECT CRUD ===========

    @GetMapping
    public String projects(@SessionAttribute("userId") int currentUserId, Model model) {
        List<Project> projects = projectService.getProjectsByOwnerId(currentUserId);
        List<Project> assignedProjects = projectService.getAssignedProjectsByUserId(currentUserId);

        model.addAttribute("projects", projects);
        model.addAttribute("assignedProjects", assignedProjects);

        return "projects";
    }

    @GetMapping("/{projectId}")
    public String showProject(@PathVariable int projectId, @SessionAttribute("userId") int currentUserId, Model model) {
        // Check if the user has access to the project
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            throw new AccessDeniedException("You do not have access to this project.");
        }

        Project project = projectService.getProjectWithTree(projectId);
        ProjectRole projectRole = projectService.getUserRole(projectId, currentUserId);
        String userRole = projectRole != null ? projectRole.getRole() : "";

        model.addAttribute("project", project);
        model.addAttribute("userRole", userRole);

        return "project";
    }

    @GetMapping("/{projectId}/hour_distribution")
    public String showHourDistribution(@PathVariable int projectId,
                                       @SessionAttribute("userId") int currentUserId,
                                       Model model) {
        // Check if the user has access to the project
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            throw new AccessDeniedException("You do not have access to this project.");
        }

        Project project = projectService.getProjectWithTree(projectId);
        Map<LocalDate, Double> hourDistributionMap = hourDistributionService.getProjectHourDistribution(project);

        model.addAttribute("project", project);
        model.addAttribute("hourDistributionMap", hourDistributionMap);

        return "project_hour_distribution";
    }

    @GetMapping("/create")
    public String showCreateProjectForm(@SessionAttribute("userId") int currentUserId, Model model) {
        Project newProject = new Project();
        newProject.setOwnerId(currentUserId);
        newProject.setStartDate(LocalDate.now());
        newProject.setEndDate(LocalDate.now());

        model.addAttribute("newProject", ProjectDTO.fromEntity(newProject));
        return "project_registration_form";
    }

    @PostMapping("/create")
    public String createProject(@Valid @ModelAttribute("newProject") ProjectDTO projectDTO,
                                BindingResult bindingResult,
                                Model model) {
        boolean fieldsHaveErrors = bindingResult.hasErrors();

        //if validation failed, return to form
        if (fieldsHaveErrors) {
            model.addAttribute("newProject", projectDTO);
            return "project_registration_form";
        }

        int projectId = projectService.createProject(projectDTO.toEntity());

        return "redirect:/projects/" + projectId;
    }

    @GetMapping("/{parentId}/create")
    public String showCreateSubProjectForm(@PathVariable int parentId, @SessionAttribute("userId") int currentUserId, Model model) {
        if (!projectService.hasAccessToProject(parentId, currentUserId)) {
            throw new AccessDeniedException("You do not have access to this project.");
        }
        Project subProject = projectService.prepareSubProject(parentId);

        model.addAttribute("newProject", ProjectDTO.fromEntity(subProject));
        return "project_registration_form";
    }

    @GetMapping("/{projectId}/edit")
    public String showEditProjectForm(@PathVariable int projectId,
                                      @SessionAttribute("userId") int currentUserId,
                                      Model model) {
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            throw new AccessDeniedException("You do not have access to this project.");
        }
        Project project = projectService.getProject(projectId);
        ProjectRole userRole = projectService.getUserRole(projectId, currentUserId);

        boolean isOwner = project.getOwnerId() == currentUserId;
        boolean hasFullAccess = userRole != null && userRole.getRole().equals("FULL_ACCESS");

        // Only owner and full access can edit
        if (!isOwner && !hasFullAccess) {
            throw new AccessDeniedException("You do not have permission to edit this project.");
        }

        model.addAttribute("project", ProjectDTO.fromEntity(project));
        model.addAttribute("userRole", userRole != null ? userRole.getRole() : "");
        return "project_edit_form";
    }

    @PostMapping("/{projectId}/edit")
    public String updateProject(@PathVariable int projectId,
                                @Valid @ModelAttribute("project") ProjectDTO projectDTO,
                                BindingResult bindingResult,
                                @SessionAttribute("userId") int currentUserId,
                                RedirectAttributes redirectAttributes) {

        projectDTO.setProjectId(projectId);

        // Check access
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            throw new AccessDeniedException("You do not have access to this project.");
        }

        Project existingProject = projectService.getProject(projectId);
        ProjectRole userRole = projectService.getUserRole(projectId, currentUserId);

        // Only owner and full access can edit
        boolean isOwner = existingProject.getOwnerId() == currentUserId;
        boolean hasFullAccess = userRole != null && userRole.getRole().equals("FULL_ACCESS");

        if (!isOwner && !hasFullAccess) {
            throw new AccessDeniedException("You do not have permission to edit this project.");
        }

        // Keep owner ID and parent project ID unchanged
        projectDTO.setOwnerId(existingProject.getOwnerId());
        projectDTO.setParentProjectId(existingProject.getParentProjectId());

        if (bindingResult.hasErrors()) {
            return "project_edit_form";
        }

        if (projectService.updateProject(projectDTO.toEntity())) {
            redirectAttributes.addFlashAttribute("updateSuccess", true);
        }

        return "redirect:/projects/" + projectId;
    }

    @PostMapping("/{projectId}/delete")
    public String deleteProject(@PathVariable int projectId, @SessionAttribute("userId") int currentUserId) {
        // Check access
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            throw new AccessDeniedException("You do not have access to this project.");
        }

        // Only the owner can delete
        ProjectRole userRole = projectService.getUserRole(projectId, currentUserId);
        if (userRole == null || !"OWNER".equals(userRole.getRole())) {
            throw new AccessDeniedException("Only the project owner can delete the project.");
        }

        // check whether it's a subproject before deletion, for proper redirection
        Project project = projectService.getProject(projectId);
        if (project == null) {
            throw new com.example.eksamensprojekt.exceptions.ProjectNotFoundException(projectId);
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
    public String showTeam(@PathVariable int projectId,
                           @SessionAttribute("userId") int currentUserId,
                           Model model) {
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            return "redirect:/projects";
        }

        ProjectRole projectRole = projectService.getUserRole(projectId, currentUserId);
        String userRole = projectRole != null ? projectRole.getRole() : "";

        Project project = projectService.getProject(projectId);

        Map<User, ProjectRole> projectUsersWithDirectRoles = projectService.getProjectUsersWithDirectRoles(projectId);
        Map<User, ProjectRole> projectUsersWithInheritedRoles = projectService.getProjectUsersWithInheritedRoles(projectId);

        List<ProjectRole> projectRoles = projectService.getAllProjectRoles();

        //Get all users for the datalist
        List<User> allUsers = userService.getAllUsers();

        model.addAttribute("userRole", userRole);
        model.addAttribute("project", project);
        model.addAttribute("directProjectUsers", projectUsersWithDirectRoles);
        model.addAttribute("inheritedProjectUsers", projectUsersWithInheritedRoles);
        model.addAttribute("projectRoles", projectRoles);
        model.addAttribute("allUsers", allUsers);
        return "project_team";
    }

    @PostMapping("/{projectId}/team/add")
    public String addTeamMember(@PathVariable int projectId,
                                @RequestParam("email") String email,
                                @RequestParam("role") String role,
                                @SessionAttribute("userId") int currentUserId,
                                RedirectAttributes redirectAttributes) {
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
    public String updateTeamMemberRole(@PathVariable int projectId,
                                       @PathVariable int userId,
                                       @RequestParam("role") String role,
                                       @SessionAttribute("userId") int currentUserId) {

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
    public String removeTeamMember(@PathVariable int projectId,
                                   @PathVariable int userId,
                                   @SessionAttribute("userId") int currentUserId) {

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
