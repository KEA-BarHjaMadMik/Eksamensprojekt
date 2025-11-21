package com.example.eksamensprojekt.controller;

import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.service.ProjectService;
import com.example.eksamensprojekt.utils.SessionUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("projects")
public class ProjectController {
    private final ProjectService service;

    public ProjectController(ProjectService service){
        this.service = service;
    }

    /*@GetMapping()
    public String getProjects(HttpSession session, Model model){
        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        int ownerID = (int) session.getAttribute("userID");
        List<Project> projects = service.getProjects(ownerID);

        return "project_list";
    }*/

    @GetMapping("/create")
    public String createProject(HttpSession session,
                                @Valid @ModelAttribute Project project,
                                BindingResult bindingResult,
                                Model model) {

        if (!SessionUtil.isLoggedIn(session)) return "redirect:/login";

        boolean fieldsHaveErrors = bindingResult.hasErrors();

        project.setOwnerID(setProjectOwner(session));

        //if validation failed, return to form
        if (bindingResult.hasErrors()){
            return "project_registration_form";
        }

        service.createProjectAndReturnID(project);
        return "redirect:/projects/" + project.getProjectId();
    }


    //Helper methods

    private int setProjectOwner(HttpSession session){
        return (int) session.getAttribute("userID");
    }
}
