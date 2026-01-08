package com.example.eksamensprojekt.controller;

import com.example.eksamensprojekt.exceptions.AccessDeniedException;
import com.example.eksamensprojekt.model.*;
import com.example.eksamensprojekt.model.TaskDTO;
import com.example.eksamensprojekt.service.ProjectService;
import com.example.eksamensprojekt.service.TaskService;
import com.example.eksamensprojekt.service.UserService;
import com.example.eksamensprojekt.utils.SessionUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@SuppressWarnings("JvmTaintAnalysis")
@Controller
@RequestMapping("tasks")
public class TaskController {
    private final TaskService taskService;
    private final ProjectService projectService;
    private final UserService userService;

    public TaskController(TaskService taskService, ProjectService projectService, UserService userService) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.userService = userService;
    }

    // =========== TASK CRUD===========

    @GetMapping("/{taskId}")
    public String showTask(@PathVariable int taskId, Model model, @SessionAttribute("userId") int currentUserId) {

        // Check if the user has access to the task
        int projectId = taskService.getTask(taskId).getProjectId();
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            throw new AccessDeniedException("You do not have access to this task.");
        }

        // Add task and role to the model
        Task task = taskService.getTaskWithTree(taskId);
        String userRole = projectService.getUserRole(projectId, currentUserId).getRole();

        model.addAttribute("task", task);
        model.addAttribute("userRole", userRole);

        return "task";
    }

    @GetMapping("/{projectId}/create")
    public String showCreateTaskForm(@PathVariable int projectId,
                                     @SessionAttribute("userId") int currentUserId,
                                     Model model) {
        // Verify ownership / access
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            throw new AccessDeniedException("You do not have access to this project.");
        }

        //create a blank task
        Task task = taskService.prepareTask(projectId);

        model.addAttribute("task", TaskDTO.fromEntity(task));

        return "task_form";
    }

    @GetMapping("/{parentTaskId}/subtask/create")
    public String showCreateSubtaskForm(@PathVariable int parentTaskId,
                                        @SessionAttribute("userId") int currentUserId,
                                        Model model) {
        //Get parent task
        Task parentTask = taskService.getTask(parentTaskId);
        if (parentTask == null) throw new com.example.eksamensprojekt.exceptions.TaskNotFoundException(parentTaskId);

        if (!projectService.hasAccessToProject(parentTask.getProjectId(),
                currentUserId)) {
            throw new AccessDeniedException("You do not have access to this project.");
        }

        Task task = taskService.prepareSubtask(parentTaskId);

        model.addAttribute("task", TaskDTO.fromEntity(task));
        model.addAttribute("parentTask", parentTask);

        return "task_form";
    }

    @PostMapping("/create")
    public String createTask(@Valid @ModelAttribute("task") TaskDTO taskDTO,
                             BindingResult bindingResult,
                             @SessionAttribute("userId") int currentUserId,
                             Model model) {
        // Verify ownership / access
        if (!projectService.hasAccessToProject(taskDTO.getProjectId(),
                currentUserId)) {
            throw new AccessDeniedException("You do not have access to this project.");
        }
        //if validation fails, return to form
        if (bindingResult.hasErrors()) {
            //if it's a subtask, add the parent task to the model as well
            if (taskDTO.getParentTaskId() != null) {
                Task parentTask = taskService.getTask(taskDTO.getParentTaskId());
                model.addAttribute("parentTask", parentTask);
            }
            return "task_form";
        }

        //If all went successful, redirect to the result
        Task task = taskDTO.toEntity();
        taskService.createTask(task);

        //redirect depending on if it's a parent task or subtask
        if (task.isSubtask()) {
            //creates a subtask if parentId is not null and shows the parent task page
            return "redirect:/tasks/" + task.getParentTaskId();
        } else {
            //creates a parent task and shows the project page
            return "redirect:/projects/" + task.getProjectId();
        }
    }

    @GetMapping("/{taskId}/edit")
    public String showEditTaskForm(@PathVariable int taskId,
                                   Model model,
                                   @SessionAttribute("userId") int currentUserId) {
        Task task = taskService.getTask(taskId);

        // Check if the user has access to the task
        int projectId = task.getProjectId();
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            throw new AccessDeniedException("You do not have access to this task.");
        }

        // Verify the role is not READ_ONLY
        String userRole = projectService.getUserRole(projectId, currentUserId).getRole();
        if ("READ_ONLY".equals(userRole)) {
            throw new AccessDeniedException("You do not have permission to edit this task.");
        }

        List<TaskStatus> taskStatusList = taskService.getAllTaskStatuses();

        model.addAttribute("task", TaskDTO.fromEntity(task));
        model.addAttribute("userRole", userRole);
        model.addAttribute("taskStatusList", taskStatusList);

        //Get all parent tasks in the project for the dropdown
        List<Task> availableParentTasks = taskService.getAllTasksInProject(projectId);
        model.addAttribute("availableParentTasks", availableParentTasks);
        model.addAttribute("currentTaskId", taskId); // Filters out(in thymeleaf) the current task
                                                                  // from the dropdown menu
        return "task_edit_form";
    }

    @PostMapping("/edit")
    public String editTask(@Valid @ModelAttribute("task") TaskDTO taskDTO,
                           @RequestParam int statusId,
                           BindingResult bindingResult,
                           @SessionAttribute("userId") int currentUserId,
                           Model model) {
        // Check if the user has access to the task
        int projectId = taskDTO.getProjectId();
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            throw new AccessDeniedException("You do not have access to this task.");
        }

        // Verify the role is not READ_ONLY
        String userRole = projectService.getUserRole(projectId, currentUserId).getRole();
        if ("READ_ONLY".equals(userRole)) {
            throw new AccessDeniedException("You do not have permission to edit this task.");
        }

        // Bean validation errors
        if (bindingResult.hasErrors()) {
            List<TaskStatus> taskStatusList = taskService.getAllTaskStatuses();
            model.addAttribute("taskStatusList", taskStatusList);
            return "task_edit_form";
        }

        // set status
        TaskStatus status = new TaskStatus(statusId, null); // status name matching id is loaded on task retrieval from DB
        taskDTO.setStatus(status);

        // Check if a parent task has changed and validate no circular reference
        Task existingTask = taskService.getTask(taskDTO.getTaskId());
        Integer newParentTaskId = taskDTO.getParentTaskId();
        Integer oldParentTaskId = existingTask.getParentTaskId();

        // Objects.equals handles null values safely,
        // so if objects are the same value(No parent change) it skips the circular reference check
        if (!java.util.Objects.equals(newParentTaskId, oldParentTaskId)) {
            //If true, then there is a circular reference, so return to the form with an error message
            if (taskService.wouldCreateCircularReference(taskDTO.getTaskId(), newParentTaskId)) {
                model.addAttribute("error", "kan ikke flytte opgaven: Skaber cirkulær reference");

                List<TaskStatus> taskStatusList = taskService.getAllTaskStatuses();
                List<Task> availableParentTasks = taskService.getAllTasksInProject(projectId);
                model.addAttribute("taskStatusList", taskStatusList);
                model.addAttribute("availableParentTasks", availableParentTasks);
                model.addAttribute("currentTaskId", taskDTO.getTaskId());
                return "task_edit_form";
            }
        }

        // update
        taskService.updateTask(taskDTO.toEntity());

        return "redirect:/tasks/" + taskDTO.getTaskId();
    }

    @PostMapping("/{taskId}/delete")
    public String deleteTask(@PathVariable int taskId, @SessionAttribute("userId") int currentUserId){
        Task task = taskService.getTask(taskId);
        int parentId = task.getParentTaskId();
        int projectId = task.getProjectId();

        if (!projectService.hasAccessToProject(projectId, currentUserId)){
            return "redirect:/";
        }
            taskService.deleteTask(taskId);
        if (parentId != 0) {
            return "redirect:/tasks/" + parentId;
        }else{
            return "redirect:/projects/" + projectId;
        }
    }

    @GetMapping("/{taskId}/move")
    public String showMoveTaskForm(@PathVariable int taskId,
                                   Model model,
                                   @SessionAttribute("userId") int currentUserId){
        Task task = taskService.getTask(taskId);

        if (!projectService.hasAccessToProject(task.getProjectId(), currentUserId)) {
            return "redirect:/projects";
        }

        ProjectRole userRole = projectService.getUserRole(task.getProjectId(), currentUserId);

        if (userRole != null && userRole.getRole().equals("READ_ONLY")) {
            return "redirect:/tasks/" + taskId;
        }

        List<Project> moveTargets = projectService.getValidMoveTargets(task.getProjectId());

        Task taskWithTree = taskService.getTaskWithTree(taskId);
        int subtaskCount = countSubTasks(taskWithTree);

        model.addAttribute("task", task);
        model.addAttribute("moveTargets", moveTargets);
        model.addAttribute("subtaskCount", subtaskCount);

        return "task_move_form";
    }

    @PostMapping("/{taskId}/move")
    public String moveTask(@PathVariable int taskId,
                           @RequestParam int targetProjectId,
                           @SessionAttribute("userId") int currentUserId,
                           RedirectAttributes redirectAttributes){
        Task task = taskService.getTask(taskId);

        if (!projectService.hasAccessToProject(task.getProjectId(), currentUserId)) {
            return "redirect:/projects";
        }

        //Role check on the source project
        ProjectRole userRole = projectService.getUserRole(task.getProjectId(), currentUserId);
        if (userRole != null && userRole.getRole().equals("READ_ONLY")) {
            return "redirect:/tasks/" + taskId;
        }

        //Check if the user has access to the target project
        if (!projectService.hasAccessToProject(targetProjectId, currentUserId)) {
            redirectAttributes.addFlashAttribute("moveErrorMessage", "Du har ikke adgang til projektet du prøver of flytte til.");
            return "redirect:/tasks/" + taskId;
        }

        //validate that the target project is different from the current project
        if (task.getProjectId() == targetProjectId) {
            redirectAttributes.addFlashAttribute("moveErrorMessage", "Opgaven er allerede i dette projekt.");
            return "redirect:/tasks/" + taskId;
        }

        //If all checks are successful, make the move
        try {
            taskService.moveTaskToProject(taskId, targetProjectId);
            redirectAttributes.addFlashAttribute("moveSuccessMessage", "Opgaven er flyttet til det nye projekt.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("moveErrorMessage", "Der skete en fejl under flytningen af opgaven. " + e.getMessage());
        }

        return "redirect:/tasks/" + taskId;
    }

    //Helper method for subtask counting
    private int countSubTasks(Task task) {
        if (task.getSubTasks() == null || task.getSubTasks().isEmpty()) {
            return 0;
        }
        int count = task.getSubTasks().size();
        for (Task subTask : task.getSubTasks()) {
            count += countSubTasks(subTask);
        }
        return count;
    }


    // ===========TIME ENTRY MANAGEMENT===========

    @GetMapping("/{taskId}/time_entries")
    public String showTimeEntries(@PathVariable int taskId,
                                  @SessionAttribute("userId") int currentUserId,
                                  Model model) {
        Task task = taskService.getTask(taskId);
        int projectId = task.getProjectId();
        // Access check
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            return "redirect:/projects";
        }

        String userRole = projectService.getUserRole(projectId,currentUserId).getRole();

        List<TimeEntry> timeEntries = taskService.getTimeEntriesByTaskId(taskId);
        List<User> projectUsers = userService.getUsersByProjectId(projectId);

        TimeEntry newTimeEntry = new TimeEntry();
        newTimeEntry.setUserId(currentUserId);

        model.addAttribute("task", task);
        model.addAttribute("userRole", userRole);
        model.addAttribute("timeEntries", timeEntries);
        model.addAttribute("projectUsers", projectUsers);
        model.addAttribute("newTimeEntry", newTimeEntry);

        return "task_time_entries";
    }

    @PostMapping("/{taskId}/time_entries/add")
    public String addTimeEntry(@PathVariable int taskId,
                               @Valid @ModelAttribute("newTimeEntry") TimeEntry newTimeEntry,
                               BindingResult bindingResult,
                               @SessionAttribute("userId") int currentUserId,
                               Model model) {
        Task task = taskService.getTask(taskId);
        int projectId = task.getProjectId();
        // Access check
        if (!projectService.hasAccessToProject(projectId, currentUserId)) {
            return "redirect:/projects";
        }

        if (bindingResult.hasErrors()) {
            // Rebuild the model
            model.addAttribute("task", task);
            model.addAttribute("timeEntries", taskService.getTimeEntriesByTaskId(taskId));
            model.addAttribute("projectUsers", userService.getUsersByProjectId(projectId));

            return "task_time_entries";
        }

        taskService.addTimeEntry(newTimeEntry);

        return String.format("redirect:/tasks/%s/time_entries", taskId);
    }
}
