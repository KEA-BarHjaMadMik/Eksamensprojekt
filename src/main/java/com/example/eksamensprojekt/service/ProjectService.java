package com.example.eksamensprojekt.service;

import com.example.eksamensprojekt.exceptions.ProjectNotFoundException;
import com.example.eksamensprojekt.exceptions.DatabaseOperationException;
import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.model.ProjectRole;
import com.example.eksamensprojekt.model.Task;
import com.example.eksamensprojekt.model.User;
import com.example.eksamensprojekt.repository.ProjectRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskService taskService;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, TaskService taskService, UserService userService) {
        this.projectRepository = projectRepository;
        this.taskService = taskService;
        this.userService = userService;
    }

    public boolean hasAccessToProject(int projectId, int userId) {
        try {
            return hasAccessRecursive(projectId, userId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to verify project access", e);
        }
    }

    private boolean hasAccessRecursive(int projectId, int userId) {
        Project project = projectRepository.getProject(projectId);
        if (project == null) {
            return false;
        }

        // Direct access?
        if (project.getOwnerId() == userId ||
                projectRepository.isUserAssignedToProject(projectId, userId)) {
            return true;
        }

        // No parent?
        if (project.getParentProjectId() == null) {
            return false;
        }

        // Check parent access recursively
        return hasAccessRecursive(project.getParentProjectId(), userId);
    }

    public int createProject(Project project) {
        try {
            // Create the project
            int projectId = projectRepository.createProject(project);
            project.setProjectId(projectId);

            return projectId;

        } catch (DataAccessException e) {
            // Any exception rolls back transaction
            throw new DatabaseOperationException("Failed to create new project", e);
        }
    }

    public List<Project> getProjectsByOwnerId(int userId) {
        try {
            return projectRepository.getProjectsByOwnerId(userId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to get projects", e);
        }
    }

    public List<Project> getAssignedProjectsByUserId(int userId) {
        try {
            List<Project> projects = projectRepository.getAssignedProjectsByUserId(userId);

            // Build a set of all assigned project IDs for fast lookup
            Set<Integer> projectIds = projects.stream()
                    .map(Project::getProjectId)
                    .collect(Collectors.toSet());

            // Filter out projects whose parent is also assigned
            return projects.stream()
                    .filter(p -> p.getParentProjectId() == null || !projectIds.contains(p.getParentProjectId()))
                    .toList();

        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to get projects assigned to user with id=" + userId, e);
        }
    }

    public Project getProject(int projectId) {
        try {
            // Retrieve project with id
            Project project = projectRepository.getProject(projectId);

            // throw error if the project is not found
            if (project == null) {
                throw new ProjectNotFoundException(projectId);
            }

            return project;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve project with id=" + projectId, e);
        }
    }

    public Project getProjectWithTree(int projectId) {
        try {
            // Retrieve project with id
            Project project = projectRepository.getProject(projectId);

            // throw error if the project is not found
            if (project == null) {
                throw new ProjectNotFoundException(projectId);
            }

            // Load project tree setting subprojects and subtasks
            // To prevent infinite recursion, a set is added to track visited projects
            Set<Integer> visitedProjects = new HashSet<>();

            loadProjectTree(project, visitedProjects);

            return project;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve project with id=" + projectId, e);
        }
    }

    public ProjectRole getUserRole(int projectId, int userId) {
        try {
            // 1. Check direct role
            ProjectRole directRole = projectRepository.getProjectUserRole(projectId, userId);
            if (directRole != null) {
                return directRole;
            }

            // 2. Otherwise, check for an inherited role
            return getInheritedRole(projectId, userId);

        } catch (DataAccessException e) {
            throw new DatabaseOperationException(
                    "Failed to retrieve user role for projectId=" + projectId + " and userId=" + userId,
                    e
            );
        }
    }

    public List<User> getDirectProjectUsers(int projectId) {
        return userService.getUsersByProjectId(projectId);
    }

    public List<User> getInheritedProjectUsers(int projectId) {
        Set<Integer> seenUsers = new HashSet<>();
        List<User> inheritedUsers = new ArrayList<>();

        // Add direct users to seenUsers so they are skipped in inheritance
        List<User> directUsers = userService.getUsersByProjectId(projectId);
        for (User user : directUsers) {
            seenUsers.add(user.getUserId());
        }

        collectInheritedProjectUsers(projectId, seenUsers, inheritedUsers);

        return inheritedUsers;
    }

    private void collectInheritedProjectUsers(int projectId, Set<Integer> seenUsers, List<User> result) {
        Project project = projectRepository.getProject(projectId);
        if (project == null || project.getParentProjectId() == null) {
            return; // no more ancestors
        }

        int parentId = project.getParentProjectId();

        // Add users from parent who are not in seenUsers
        for (User user : getDirectProjectUsers(parentId)) {
            if (seenUsers.add(user.getUserId())) {
                result.add(user);
            }
        }

        // Recurse upward
        collectInheritedProjectUsers(parentId, seenUsers, result);
    }

    public Map<User, ProjectRole> getProjectUsersWithDirectRoles(int projectId) {
        try {
            Map<User, ProjectRole> result = new HashMap<>();

            List<User> projectUsers = getDirectProjectUsers(projectId);

            for (User user : projectUsers) {
                ProjectRole directRole = projectRepository.getProjectUserRole(projectId, user.getUserId());
                if (directRole != null) {
                    result.put(user, directRole);
                }
            }

            return result;

        } catch (DataAccessException e) {
            throw new DatabaseOperationException(
                    "Failed to retrieve direct roles for projectId=" + projectId, e
            );
        }
    }

    public Map<User, ProjectRole> getProjectUsersWithInheritedRoles(int projectId) {
        try {
            Map<User, ProjectRole> result = new HashMap<>();

            // 1. Get all users from ancestors, skipping direct users
            List<User> inheritedUsers = getInheritedProjectUsers(projectId);

            // 2. Resolve inherited roles for each user
            for (User user : inheritedUsers) {
                int userId = user.getUserId();
                ProjectRole inheritedRole = getInheritedRole(projectId, userId);
                if (inheritedRole != null) {
                    result.put(user, inheritedRole);
                }
            }

            return result;

        } catch (DataAccessException e) {
            throw new DatabaseOperationException(
                    "Failed to retrieve inherited roles for projectId=" + projectId, e
            );
        }
    }

    private ProjectRole getInheritedRole(int projectId, int userId) {
        Project project = projectRepository.getProject(projectId);
        if (project == null || project.getParentProjectId() == null) {
            return null;
        }

        int parentId = project.getParentProjectId();

        // direct role on parent?
        ProjectRole parentRole = projectRepository.getProjectUserRole(parentId, userId);
        if (parentRole != null) {
            return parentRole;
        }

        // continue up
        return getInheritedRole(parentId, userId);
    }

    public List<ProjectRole> getAllProjectRoles() {
        try {
            return projectRepository.getAllProjectRoles();
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve projectRoles", e);
        }
    }

    private void loadProjectTree(Project project, Set<Integer> visitedProjects) {

        // Prevent endless recursion by tracking visited project Ids.
        // visitedProjects.add(...) returns false if the ID was already added,
        // meaning we've already processed this project, so we stop recursing.
        if (!visitedProjects.add(project.getProjectId())) {
            return;
        }

        try {
            // Load direct subprojects
            List<Project> subProjects = projectRepository.getDirectSubProjects(project.getProjectId());
            project.setSubProjects(subProjects);

            // For each subproject, load its subprojects and tasks using recursion
            // Base case implicit: when subProjects is empty loop will not run
            for (Project sub : subProjects) {
                loadProjectTree(sub, visitedProjects);
            }
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve subprojects with parent id=" + project.getProjectId(), e);
        }

        // Load project tasks with subtasks
        List<Task> tasks = taskService.getProjectTasksWithSubtasks(project.getProjectId());
        project.setTasks(tasks);
    }

    public void deleteProject(int projectId) {
        try {
            int rowsAffected = projectRepository.deleteProject(projectId);
            if (rowsAffected == 0) throw new ProjectNotFoundException(projectId);
            //project deleted if at least 1 row is affected
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to delete project", e);
        }
    }

    public void addUserToProject(int projectId, String email, String role) {
        try {
            int userId = userService.getUserByEmail(email).getUserId();
            projectRepository.addUserToProject(projectId, userId, role);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to add user to project", e);
        }
    }

    public void updateUserRole(int projectId, int userId, String role) {
        try {
            projectRepository.updateUserRole(projectId, userId, role);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to update user's project role", e);
        }
    }

    public void removeUserFromProject(int projectId, int userId) {
        try {
            projectRepository.removeUserFromProject(projectId, userId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to remove user from project", e);
        }
    }

    public boolean updateProject(Project updatedProject) {
        try {
            int rowsAffected = projectRepository.updateProject(updatedProject);
            if (rowsAffected == 0) throw new ProjectNotFoundException(updatedProject.getProjectId());
            return true; // project updated
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to update project", e);
        }
    }
}