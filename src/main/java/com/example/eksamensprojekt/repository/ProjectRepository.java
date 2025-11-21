package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.Project;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;

@Repository
public class ProjectRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProjectRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createProjectAndReturnID(Project project){
        String sql = "INSERT INTO project (owner_id, parent_project_id, title, description, start_date, end_date) VALUES (?,?,?,?,?,?)";

        jdbcTemplate.update(
                sql,
                project.getOwnerID(),
                project.getParentProjectId(),
                project.getTitle(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate()
        );
    }
}
