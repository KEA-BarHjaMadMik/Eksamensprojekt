package com.example.eksamensprojekt.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProjectNotFoundException extends RuntimeException{
    public ProjectNotFoundException(int projectID) {
        super("Project not found: id=" + projectID);
    }
}