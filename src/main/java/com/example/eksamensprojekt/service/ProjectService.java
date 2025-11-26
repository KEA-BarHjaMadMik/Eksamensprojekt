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
            return project.getOwnerID() == userId || projectRepository.isUserAssignedToProject(projectId, userId);
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

    public List<Project> getProjectsByOwnerID(int userId) {
        try {
            return projectRepository.getProjectsByOwnerID(userId);
        } catch (DataAccessException e){
            throw new DatabaseOperationException("Failed to get projects", e);
        }
    }

    public Project getProject(int projectID) {
        try {
            // Retrieve project with id
            Project project = projectRepository.getProject(projectID);

            // throw error if the project is not found
            if (project == null) {
                throw new ProjectNotFoundException(projectID);
            }

            // Load project tree setting subprojects and subtasks
            // To prevent infinite recursion, a set is added to track visited projects
            Set<Integer> visitedProjects = new HashSet<>();

            loadProjectTree(project, visitedProjects);

            return project;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve project with id=" + projectID, e);
        }
    }

    public String getUserRole(Project project, int userId) {
        // early exit if the owner
        if (project.getOwnerID() == userId) {
            return "OWNER";
        }

        try {
            return projectRepository.getProjectUserRole(project.getProjectId(), userId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve user role for project with id=" + project.getProjectId() + "and user with id=" + userId, e);
        }
    }

    private void loadProjectTree(Project project, Set<Integer> visitedProjects) {

        // Prevent endless recursion by tracking visited project IDs.
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
}