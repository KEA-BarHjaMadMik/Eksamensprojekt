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

    public void createProject(Project project) {
        try {
            projectRepository.createProject(project);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to create new project", e);
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
            throw new DatabaseOperationException("Failed to retrieve project, id=" + projectID, e);
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
            throw new DatabaseOperationException("Failed to retrieve subprojects, parent id=" + project.getProjectId(), e);
        }

        // Load project tasks with subtasks
        List<Task> tasks = taskService.getProjectTasksWithSubtasks(project.getProjectId());
        project.setTasks(tasks);
    }
}