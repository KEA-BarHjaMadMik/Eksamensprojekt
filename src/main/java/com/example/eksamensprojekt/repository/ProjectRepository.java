package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.Project;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class ProjectRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int createProject(Project project) {
        String sql = "INSERT INTO project (owner_id, parent_project_id, title, description, start_date, end_date) VALUES (?,?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, project.getOwnerID());
                    ps.setInt(2, project.getParentProjectId());
                    ps.setString(3, project.getTitle());
                    ps.setString(4, project.getDescription());
                    ps.setDate(5, Date.valueOf(project.getStartDate()));
                    ps.setDate(6, Date.valueOf(project.getEndDate()));
                    return ps;
                },
                keyHolder
        );
        Number projectID = keyHolder.getKey();

        String sql2 = "INSERT INTO project_users(project_id, user_id, role_id) VALUES(?,?,?)";

        jdbcTemplate.update(sql2, project.getProjectId(), project.getOwnerID(), 1); //1 is owner role id

        return (projectID != null) ? projectID.intValue() : -1;
    }

    public List<Project> getProjectsByOwnerID(int ownerID) {
        String sql = """
                SELECT project_id, owner_id, parent_project_id, title, description, start_date, end_date
                FROM project
                WHERE owner_id = ? AND parent_project_id IS NULL
                """;

        return jdbcTemplate.query(sql, getProjectRowMapper(), ownerID);
    }

    public Project getProject(int projectID) {
        String sql = """
                SELECT project_id, owner_id, parent_project_id, title, description, start_date, end_date
                FROM project
                WHERE project_id = ?
                """;
        List<Project> results = jdbcTemplate.query(sql, getProjectRowMapper(), projectID);
        return results.isEmpty() ? null : results.getFirst();
    }

    public List<Project> getDirectSubProjects(int parentProjectID) {
        String sql = """
                SELECT project_id, owner_id, parent_project_id, title, description, start_date, end_date
                FROM project
                WHERE parent_project_id = ?
                """;

        return jdbcTemplate.query(sql, getProjectRowMapper(), parentProjectID);
    }

    private RowMapper<Project> getProjectRowMapper() {
        return ((rs, rowNum) -> new Project(
                rs.getInt("project_id"),
                rs.getInt("owner_id"),
                rs.getInt("parent_project_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null,
                rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null,
                new ArrayList<>(),
                new ArrayList<>())
        );
    }
}
