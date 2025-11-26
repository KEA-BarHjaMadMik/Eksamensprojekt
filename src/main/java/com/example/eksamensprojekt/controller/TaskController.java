package com.example.eksamensprojekt.controller;

import com.example.eksamensprojekt.model.Project;
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
        //Get project and verify ownership
        Project project = projectService.getProject(projectId);
        if (isProjectOwner(session, project)) return "redirect:/";

        //create a blank task
        Task task = new Task();
        task.setProjectId(projectId);
        task.setParentTaskId(0); ///overflødig

        model.addAttribute("task", task);
        model.addAttribute("project", project); ///?


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

        Project project = projectService.getProject(parentTask.getProjectId());
        if (isProjectOwner(session, project)) return "redirect:/";

        Task task = new Task();
        task.setProjectId(parentTask.getProjectId());
        task.setParentTaskId(parentTaskId);

        model.addAttribute("task", task);
        model.addAttribute("parentTask", parentTask); ///?
        model.addAttribute("project", project); ///?

        return "task_form";
    }

    @PostMapping("/create")
    public String createTask(@Valid @ModelAttribute Task task,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model) {

        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        //Get project and verify ownership
        Project project = projectService.getProject(task.getProjectId());
        if (!isProjectOwner(session, project)) return "redirect:/";

        //if validation fails return to form
        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project); ///?
            //if it's a subtask, add parent task to model as well
            if (task.getParentTaskId() != 0) {
                Task parentTask = taskService.getTask(task.getParentTaskId());
                model.addAttribute("parentTask", parentTask);
            }
            return "task_form";
        }

        //If all went successful, redirect to the result
        boolean success = taskService.createTask(task);
        if (success) {
            //redirect depending on if it's a parent task or subtask
            if (task.getParentTaskId() != 0) {
                //creates a subtask if parentId is not 0 and shows the parent task page
                return "redirect:/tasks/" + task.getParentTaskId();
            } else {
                //creates a parent task and shows the project page
                return "redirect:/projects/" + task.getProjectId();
            }
        } else {
            //if creation failed
            model.addAttribute("saveFailure", true);
            model.addAttribute("project", project);
            return "task_form";
        }

    }

    //Helper methods
    private int getCurrentUserId(HttpSession session){
        return (int) session.getAttribute("userID");
    }

    private boolean isProjectOwner(HttpSession session, Project project) {
        return project != null && getCurrentUserId(session) == project.getOwnerID();
    }
}
