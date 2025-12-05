package com.example.eksamensprojekt.service;

import com.example.eksamensprojekt.exceptions.DatabaseOperationException;
import com.example.eksamensprojekt.exceptions.TaskNotFoundException;
import com.example.eksamensprojekt.model.Task;
import com.example.eksamensprojekt.model.TaskStatus;
import com.example.eksamensprojekt.model.TimeEntry;
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
            Task task = taskRepository.getTask(taskId);

            if (task == null) {
                throw new TaskNotFoundException(taskId);
            }

            return task;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve task", e);
        }
    }

    public Task getTaskWithTree(int taskId) {
        try {
            Task task = taskRepository.getTask(taskId);

            if (task == null) {
                throw new TaskNotFoundException(taskId);
            }

            Set<Integer> visitedTasks = new HashSet<>();
            loadTaskTree(task, visitedTasks);

            return task;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve task with id=" + taskId, e);
        }
    }

    public List<TaskStatus> getAllTaskStatuses() {
        try{
            return taskRepository.getAllTaskStatuses();
        }catch (DataAccessException e){
            throw new DatabaseOperationException("failed to retrieve all task statuses", e);
        }
    }

    public void createTask(Task task) {
        //It works but should probably add validation and exception handling
        try {
            taskRepository.createTask(task);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Database error during task creation", e);
        }
    }

    public void updateTask(Task task) {
        try{
            taskRepository.updateTask(task);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Database error during task update", e);
        }
    }

    private void loadTaskTree(Task task, Set<Integer> visitedTasks) {

        // Prevent endless recursion by tracking visited task Ids.
        // visitedTasks.add(...) returns false if the Id was already added,
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

    public void deleteTask(int taskId){
        try {
            int rowsAffected = taskRepository.deleteTask(taskId);
            if (rowsAffected == 0) throw new TaskNotFoundException(taskId);
            //task deleted if at least 1 row is affected
        }catch (DataAccessException e){
            throw new DatabaseOperationException("Failed to delete task with id " + taskId, e);
        }
    }

    public List<TimeEntry> getTimeEntriesByTaskId(int taskId) {
        try {
            return taskRepository.getTimeEntriesByTaskId(taskId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve task entries for task with id " + taskId, e);
        }
    }

    public void addTimeEntry(TimeEntry newTimeEntry){
        try {
            taskRepository.createTimeEntry(newTimeEntry);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to insert new time entry", e);
        }
    }

    public List<Task> getAllTasksInProject(int projectId){
        try {
            return taskRepository.getAllTasksInProject(projectId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve tasks for project with id " + projectId, e);
        }
    }

    //Returns true if moving the task would create a circular reference
    public boolean wouldCreateCircularReference(int taskId, Integer newParentId){
        if (newParentId == null) {return false;} //top level task is safe
        if (newParentId == taskId) {return true;} //Can't be own parent

        //Get all descendant taskIds of the task and check if newParentId is in it
        Set<Integer> descendantset = getDescendantTaskId(taskId);
        // .contains is a hashset method that checks if a value exists in the set
        return descendantset.contains(newParentId); //Returns true if newParentId is in descendantset
    }

    public void moveTaskToProject(int taskId, int targetProjectId) {
        Task task = getTask(taskId);
        moveTaskAndSubtasksToProject(task ,targetProjectId);
    }

    //Recursive Helper methods
    //Recursively moves tasks and all subtasks to targetProjectId
    private void moveTaskAndSubtasksToProject(Task task, int targetProjectId){
        taskRepository.updateTaskProjectId(task.getTaskId(), targetProjectId);

        //Get subtasks from DB and move them recursively
        List<Task> subTasks = taskRepository.getSubTasks(task.getTaskId());
        for (Task subTask : subTasks) {
            moveTaskAndSubtasksToProject(subTask, targetProjectId);
        }
    }

    //Gets all descendant taskIds of a task recursively
    private Set<Integer> getDescendantTaskId(int taskId){
        //HashSet to prevent duplicates
        Set<Integer> descendants = new HashSet<>();

        //get all and ONLY direct children from DB
        List<Task> subTasks = taskRepository.getSubTasks(taskId);

        for (Task subTask : subTasks) {
            //Add the subtask ID
            descendants.add(subTask.getTaskId());
            //add all of its descendants recursively
            descendants.addAll(getDescendantTaskId(subTask.getTaskId()));
        }
        return descendants;
    }
}
