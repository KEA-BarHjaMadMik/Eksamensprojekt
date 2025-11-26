package com.example.eksamensprojekt.service;

import com.example.eksamensprojekt.exceptions.DatabaseOperationException;
import com.example.eksamensprojekt.model.Task;
import com.example.eksamensprojekt.repository.TaskRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getProjectTasksWithSubtasks(int projectId) {
        try {
            // Load direct project tasks
            List<Task> tasks = taskRepository.getDirectProjectTasks(projectId);

            // For each task, load project tree
            // To prevent infinite recursion a set is added to track visited tasks
            Set<Integer> visitedTasks = new HashSet<>();
            for (Task task : tasks) {
                loadTaskTree(task, visitedTasks);
            }

            return tasks;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("failed to retrieve tasks for project with id " + projectId, e);
        }
    }

    //getTask is just for authorization checks
    //and getting parent task info
    public Task getTask(int taskId) {
        try {
            return taskRepository.getTask(taskId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve task", e);
        }
    }

    public boolean createTask(Task task) {
        //It works but should probably add validation and exception handling
        try {
        return taskRepository.createTask(task);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Database error during task creation", e);
        }
    }

    private void loadTaskTree(Task task, Set<Integer> visitedTasks) {

        // Prevent endless recursion by tracking visited task IDs.
        // visitedTasks.add(...) returns false if the ID was already added,
        // meaning we've already processed this project, so we stop recursing.
        if (!visitedTasks.add(task.getTaskId())) {
            return;
        }

        try {
            // Load direct subtasks
            List<Task> subTasks = taskRepository.getSubTasks(task.getTaskId());
            task.setSubTasks(subTasks);

            // For each subtask, load its subtasks using recursion
            // Base case implicit: when subTasks is empty loop will not run
            for (Task subTask : subTasks) {
                loadTaskTree(subTask, visitedTasks);
            }
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("failed to retrieve subtasks for task with id " + task.getTaskId(), e);
        }
    }
}
