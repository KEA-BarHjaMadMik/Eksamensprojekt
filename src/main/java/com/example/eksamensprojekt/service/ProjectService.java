package com.example.eksamensprojekt.service;

import com.example.eksamensprojekt.exceptions.DatabaseOperationException;
import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.repository.ProjectRepository;
import com.example.eksamensprojekt.repository.UserRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProjectService {
    ProjectRepository repository;

    public ProjectService(ProjectRepository repository){
        this.repository = repository;
    }

    /*public List<Project> getProjects(int ownerID){
        try {
            List<Project> projects = repository.getProjects(ownerID);
            return projects;
        } catch (DataAccessException e){
            throw new DatabaseOperationException("Failed to retrieve user", e);
        }

    }*/
}
