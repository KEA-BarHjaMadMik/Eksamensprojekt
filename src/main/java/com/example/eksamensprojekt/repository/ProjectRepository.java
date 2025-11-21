package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.Project;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProjectRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProjectRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    /*public List<Project> getProjects(int ownerID){
        String sql = "SELECT project_id, parent_project_id, title, description, start_date, end_date FROM project where ownerID = ?";

    }

    private RowMapper<Project> projectRowMapper(){
        return ((rs, rowNum) -> new Project(
                rs.getInt("project_id"),
                rs.getInt("owner_id"),
                rs.getInt("parent_project_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getDate("start_date").toLocalDate(),
                rs.getDate("end_date").toLocalDate()
    }*/
}
