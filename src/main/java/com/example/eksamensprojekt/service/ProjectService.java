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

import java.time.LocalDate;
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

    // ===========PROJECT ===========
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

            // Load all projects in the tree and build the structure in-memory
            // to avoid N+1 problem.
            List<Project> allProjectsInTree = new ArrayList<>();
            allProjectsInTree.add(project);
            collectAllSubProjects(project, allProjectsInTree, new HashSet<>());

            // Get all project IDs to batch load tasks
            List<Integer> projectIds = allProjectsInTree.stream()
                    .map(Project::getProjectId)
                    .collect(Collectors.toList());

            // Batch load all tasks for these projects
            List<Task> allTasks = taskService.getTasksForProjects(projectIds);

            // Organize tasks by project ID
            Map<Integer, List<Task>> tasksByProject = allTasks.stream()
                    .collect(Collectors.groupingBy(Task::getProjectId));

            // Assign tasks to projects
            for (Project p : allProjectsInTree) {
                p.setTasks(tasksByProject.getOrDefault(p.getProjectId(), new ArrayList<>()));
            }

            return project;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve project with id=" + projectId, e);
        }
    }

    private void collectAllSubProjects(Project parent, List<Project> allProjects, Set<Integer> visited) {
        if (!visited.add(parent.getProjectId())) return;

        List<Project> subs = projectRepository.getDirectSubProjects(parent.getProjectId());
        parent.setSubProjects(subs);
        for (Project sub : subs) {
            allProjects.add(sub);
            collectAllSubProjects(sub, allProjects, visited);
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

    public void deleteProject(int projectId) {
        try {
            int rowsAffected = projectRepository.deleteProject(projectId);
            if (rowsAffected == 0) throw new ProjectNotFoundException(projectId);
            //project deleted if at least 1 row is affected
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to delete project", e);
        }
    }

    public List<Project> getValidMoveTargets(int currentProjectId) {
        try {
            //Empty list
            List<Project> targets = new ArrayList<>();
            //Get current project
            Project currentProject = projectRepository.getProject(currentProjectId);
            //Add parent project if it exsits
            Integer parentProjectId = currentProject.getParentProjectId();
            if (parentProjectId != null) {
                Project parent = projectRepository.getProject(parentProjectId);
                if (parent != null) {
                    targets.add(parent);
                }
                //add sibling projects
                List<Project> siblings = projectRepository.getDirectSubProjects(parentProjectId);
                for (Project sibling : siblings) {
                    if (sibling.getProjectId() != currentProjectId) {
                        targets.add(sibling);
                    }
                }
            }
            //add direct children (subprojects of current project)
            List<Project> children = projectRepository.getDirectSubProjects(currentProjectId);
            targets.addAll(children);
            return targets;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve valid move targets for project with id=" + currentProjectId, e);
        }
    }

    public Project prepareSubProject(int parentId) {
        Project parent = getProject(parentId);
        if (parent == null) throw new ProjectNotFoundException(parentId);

        Project subProject = new Project();
        subProject.setOwnerId(parent.getOwnerId());
        subProject.setParentProjectId(parentId);
        subProject.setStartDate(LocalDate.now());
        subProject.setEndDate(LocalDate.now());

        return subProject;
    }

    // ===========ACCESS AND ROLES===========
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
}