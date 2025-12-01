package com.example.eksamensprojekt.service;

import com.example.eksamensprojekt.exceptions.ProjectNotFoundException;
import com.example.eksamensprojekt.exceptions.DatabaseOperationException;
import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.model.Task;
import com.example.eksamensprojekt.repository.ProjectRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskService taskService;

    public ProjectService(ProjectRepository projectRepository, TaskService taskService) {
        this.projectRepository = projectRepository;
        this.taskService = taskService;
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

    public int createProject(Project project) {
        try {
            return projectRepository.createProject(project);
        } catch (DataAccessException e) {
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

    public String getUserRole(int projectId, int userId) {
        try {
            return projectRepository.getProjectUserRole(projectId, userId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve user role for project with id=" + projectId + "and user with id=" + userId, e);
        }
    }

    private void loadProjectTree(Project project, Set<Integer> visitedProjects) {

        // Prevent endless recursion by tracking visited project Ids.
        // visitedProjects.add(...) returns false if the Id was already added,
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

    public void deleteProject(int projectId){
        try {
            int rowsAffected = projectRepository.deleteProject(projectId);
            if (rowsAffected == 0) throw new ProjectNotFoundException(projectId);
            //project deleted if at least 1 row is affected
        } catch (DataAccessException e){
            throw new DatabaseOperationException("Failed to delete project", e);
        }
    }
}