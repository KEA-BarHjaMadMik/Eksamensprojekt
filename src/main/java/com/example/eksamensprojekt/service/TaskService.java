package com.example.eksamensprojekt.service;

import com.example.eksamensprojekt.model.Task;
import com.example.eksamensprojekt.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> getProjectTasksWithSubtasks(int projectId) {
        // Load direct project tasks
        List<Task> tasks = repository.getDirectProjectTasks(projectId);

        // For each task, load project tree
        // To prevent infinite recursion a set is added to track visited tasks
        Set<Integer> visitedTasks = new HashSet<>();
        for (Task task : tasks) {
            loadTaskTree(task, visitedTasks);
        }

        return tasks;
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
        for (Task subTask : subTasks) {
            loadTaskTree(subTask, visitedTasks);
        }
    }
}
