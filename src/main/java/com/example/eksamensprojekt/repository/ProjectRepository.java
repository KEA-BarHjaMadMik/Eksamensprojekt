package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.Project;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ProjectRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createProject(Project project) {
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

    public List<Project> getProjectsByOwnerID(int ownerID) {
        String sql = """
                SELECT project_id, owner_id, parent_project_id, title, description, start_date, end_date
                FROM project
                WHERE owner_id = ? AND parent_project_id IS NULL
                """;

        return jdbcTemplate.query(sql, getProjectRowMapper(), ownerID);
    }

    public List<Project> getAssignedProjectsByUserId(int userId) {
        String sql = """
                SELECT
                    p.project_id,
                    p.owner_id,
                    p.parent_project_id,
                    p.title,
                    p.description,
                    p.start_date,
                    p.end_date
                FROM
                    project p
                        JOIN
                    project_users pu ON p.project_id = pu.project_id
                WHERE
                    pu.user_id = ? AND p.owner_id != ?
                        AND p.parent_project_id IS NULL
                """;

        return jdbcTemplate.query(sql, getProjectRowMapper(), userId, userId);
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

    public boolean isUserAssignedToProject(int projectId, int userId) {
        String sql = "SELECT COUNT(*) FROM project_users WHERE project_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, projectId, userId);
        return count != null && count > 0;
    }

    public String getProjectUserRole(int projectId, int userId) {
        String sql = """
                SELECT role
                FROM project_users
                WHERE project_id = ? AND user_id = ?
                """;

        return jdbcTemplate.queryForObject(sql, String.class, projectId, userId);
    }
}
