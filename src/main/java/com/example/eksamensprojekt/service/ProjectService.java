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
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
            Project project = projectRepository.getProject(projectId);
            if (project == null) {
                return false;
            }
            // Access granted if user is Owner OR is assigned to the project
            return project.getOwnerId() == userId || projectRepository.isUserAssignedToProject(projectId, userId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to verify project access", e);
        }
    }

    @Transactional
    public int createProject(Project project, boolean copyTeam, int creatorId) {
        try {
            // Create project
            int projectId = projectRepository.createProject(project);

            // Set project ID on object for later use
            project.setProjectId(projectId);

            // Copy team from parent if requested
            if (copyTeam) {
                copyTeamFromParent(project);
            }
            // If not copying team, but creator is not owner, add them with their role from parent
            else if (creatorId != project.getOwnerId()) {
                ProjectRole role = getUserRole(project.getParentProjectId(), creatorId);
                projectRepository.addUserToProject(projectId, creatorId, role.getRole());
            }

            return projectId;

        } catch (DataAccessException e) {
            // Any exception rolls back transaction
            throw new DatabaseOperationException("Failed to create new project", e);
        }
    }

    private void copyTeamFromParent(Project project) {
        Integer parentId = project.getParentProjectId();

        if(parentId != null) {
            Map<User, ProjectRole> usersWithRoles = getProjectUsersWithRoles(parentId);

            for (var entry : usersWithRoles.entrySet()) {
                User user = entry.getKey();

                // Skip owner (Automatically added on project creation)
                if (user.getUserId() == project.getOwnerId()) continue;

                addUserToProject(
                        project.getProjectId(),
                        user.getEmail(),
                        entry.getValue().getRole()
                );
            }
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
            return projectRepository.getAssignedProjectsByUserId(userId);
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
            return projectRepository.getProjectUserRole(projectId, userId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve user role for project with id=" + projectId + "and user with id=" + userId, e);
        }
    }

    public List<User> getProjectUsers(int projectId) {
        return userService.getUsersByProjectId(projectId);
    }

    public Map<User, ProjectRole> getProjectUsersWithRoles(int projectId) {
        try {
            Map<User, ProjectRole> projectUsersWithRoles = new HashMap<>();
            List<User> projectUsers = userService.getUsersByProjectId(projectId);
            for (User user : projectUsers) {
                ProjectRole projectRole = projectRepository.getProjectUserRole(projectId, user.getUserId());
                projectUsersWithRoles.put(user, projectRole);
            }
            return projectUsersWithRoles;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve users with projectId=" + projectId, e);
        }
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
        projectRepository.updateUserRole(projectId, userId, role);
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