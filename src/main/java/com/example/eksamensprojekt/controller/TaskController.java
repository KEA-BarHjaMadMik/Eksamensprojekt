package com.example.eksamensprojekt.controller;

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

@Controller
@RequestMapping("tasks")
public class TaskController {
    private final TaskService taskService;
    private final ProjectService projectService;

    public TaskController(TaskService taskService, ProjectService projectService) {
        this.taskService = taskService;
        this.projectService = projectService;
    }

    @GetMapping("/project/{projectId}/new")
    public String showCreateTaskForm(@PathVariable int projectId,
                                     HttpSession session,
                                     Model model) {
        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";
        // Verify ownership / access
        if (!projectService.hasAccessToProject(projectId, SessionUtil.getCurrentUserId(session))) return "redirect:/";

        //create a blank task
        Task task = new Task();
        task.setProjectId(projectId);

        model.addAttribute("task", task);

        return "task_form";
    }

    @GetMapping("/{parentTaskId}/subtask/new")
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
