package com.example.eksamensprojekt.service;

import com.example.eksamensprojekt.exceptions.DatabaseOperationException;
import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.repository.ProjectRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
    ProjectRepository repository;

    public ProjectService(ProjectRepository repository){
        this.repository = repository;
    }

    public void createProjectAndReturnID(Project project){
        try {

            repository.createProjectAndReturnID(project);
        } catch (DataAccessException e){
            throw new DatabaseOperationException("Failed to create new project", e);
        }
    }
}
