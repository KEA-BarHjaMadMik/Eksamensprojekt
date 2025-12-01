package com.example.eksamensprojekt.controller;

import com.example.eksamensprojekt.model.ProjectRole;
import com.example.eksamensprojekt.model.Task;
import com.example.eksamensprojekt.service.ProjectService;
import com.example.eksamensprojekt.service.TaskService;
import com.example.eksamensprojekt.utils.SessionUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("tasks")
public class TaskController {
    private final TaskService taskService;
    private final ProjectService projectService;

    public TaskController(TaskService taskService, ProjectService projectService) {
        this.taskService = taskService;
        this.projectService = projectService;
    }

    @GetMapping("/{taskId}")
    public String showTask(@PathVariable("taskId") int taskId, Model model, HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        // Check if the user has access to the task
        int currentUserId = SessionUtil.getCurrentUserId(session);
        int projectId = taskService.getTask(taskId).getProjectId();
        if(!projectService.hasAccessToProject(projectId, currentUserId)){
            return "redirect:/projects";
        }

        // Add task and role to the model
        Task task = taskService.getTaskWithTree(taskId);
        ProjectRole userRole = projectService.getUserRole(projectId, currentUserId);

        model.addAttribute("task", task);
        model.addAttribute("userRole", userRole);

        return "task";
    }

    @GetMapping("/{projectId}/create")
    public String showCreateTaskForm(@PathVariable int projectId,
                                     HttpSession session,
                                     Model model) {
        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";
        // Verify ownership / access
        if (!projectService.hasAccessToProject(projectId, SessionUtil.getCurrentUserId(session))) return "redirect:/";

        //create a blank task
        Task task = new Task();
        task.setProjectId(projectId);
        task.setStartDate(LocalDate.now());
        task.setEndDate(LocalDate.now());

        model.addAttribute("task", task);

        return "task_form";
    }

    @GetMapping("/{parentTaskId}/subtask/create")
    public String showCreateSubtaskForm(@PathVariable int parentTaskId,
                                        HttpSession session,
                                        Model model) {
        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        //Get parent task
        Task parentTask = taskService.getTask(parentTaskId);
        if (parentTask == null) return "redirect:/";

        if (!projectService.hasAccessToProject(parentTask.getProjectId(),
                SessionUtil.getCurrentUserId(session))) return "redirect:/";

        Task task = new Task();
        task.setProjectId(parentTask.getProjectId());
        task.setParentTaskId(parentTaskId);
        task.setStartDate(LocalDate.now());
        task.setEndDate(LocalDate.now());

        model.addAttribute("task", task);
        model.addAttribute("parentTask", parentTask); /// for subtask form, show parent task context

        return "task_form";
    }

    @PostMapping("/create")
    public String createTask(@Valid @ModelAttribute Task task,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model) {

        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        // Verify ownership / access
        if (!projectService.hasAccessToProject(task.getProjectId(),
                SessionUtil.getCurrentUserId(session))) return "redirect:/";

        //if validation fails, return to form
        if (bindingResult.hasErrors()) {
            //if it's a subtask, add the parent task to the model as well
            if (task.isSubtask()) {
                Task parentTask = taskService.getTask(task.getParentTaskId());
                model.addAttribute("parentTask", parentTask);
            }
            return "task_form";
        }

        //If all went successful, redirect to the result
        taskService.createTask(task);

        //redirect depending on if it's a parent task or subtask
        if (task.isSubtask()) {
            //creates a subtask if parentId is not 0 and shows the parent task page
            return "redirect:/tasks/" + task.getParentTaskId();
        } else {
            //creates a parent task and shows the project page
                return "redirect:/projects/" + task.getProjectId();
        }
    }
}
