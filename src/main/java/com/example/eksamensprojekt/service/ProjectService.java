package com.example.eksamensprojekt.service;

import com.example.eksamensprojekt.exceptions.ProjectNotFoundException;
import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.model.Task;
import com.example.eksamensprojekt.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectService {

    private final ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    public Project getProject(int projectID) {
        // Retrieve project with id
        Project project = repository.getProject(projectID);

        // throw error if the project is not found
        if (project == null) {
            throw new ProjectNotFoundException(projectID);
        }

        // Load project tree setting subprojects and subtasks
        // To prevent infinite recursion sets are added to track visited projects and tasks
        Set<Integer> visitedProjects = new HashSet<>();
        Set<Integer> visitedTasks = new HashSet<>();

        loadProjectTree(project, visitedProjects, visitedTasks);

        return project;
    }

    private void loadProjectTree(Project project, Set<Integer> visitedProjects, Set<Integer> visitedTasks) {

        // Prevent endless recursion by tracking visited project IDs.
        // visitedProjects.add(...) returns false if the ID was already added,
        // meaning we've already processed this project, so we stop recursing.
        if (!visitedProjects.add(project.getProjectId())) {
            return;
        }

        // Load direct subprojects
        List<Project> subProjects = repository.getDirectSubProjects(project.getProjectId());
        project.setSubProjects(subProjects);

        // For each subproject, load its subprojects and tasks using recursion
        // Base case implicit: when subProjects is empty loop will not run
        for (Project sub : subProjects) {
            loadProjectTree(sub, visitedProjects, visitedTasks);
        }

        // Load direct project tasks
        List<Task> tasks = repository.getDirectProjectTasks(project.getProjectId());
        project.setTasks(tasks);

        // For each task, load its subtasks
        for (Task task : tasks) {
            loadTaskTree(task, visitedTasks);
        }
    }

    private void loadTaskTree(Task task, Set<Integer> visitedTasks) {

        // Prevent endless recursion by tracking visited task IDs.
        // visitedTasks.add(...) returns false if the ID was already added,
        // meaning we've already processed this project, so we stop recursing.
        if (!visitedTasks.add(task.getTaskId())) {
            return;
        }

        // Load direct subtasks
        List<Task> subTasks = repository.getSubTasks(task.getTaskId());
        task.setSubTasks(subTasks);

        // For each subtask, load its subtasks using recursion
        // Base case implicit: when subTasks is empty loop will not run
        for (Task st : subTasks) {
            loadTaskTree(st, visitedTasks);
        }
    }
}