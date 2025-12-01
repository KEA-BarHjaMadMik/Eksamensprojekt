package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.Project;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Types;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class ProjectRepository {
    private static final String BASE_PROJECT_SQL = """
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
            """;

    private final JdbcTemplate jdbcTemplate;

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Project> getProjectsByOwnerId(int ownerId) {
        String sql = BASE_PROJECT_SQL + "WHERE owner_id = ? AND parent_project_id IS NULL";

        return jdbcTemplate.query(sql, getProjectRowMapper(), ownerId);
    }

    public List<Project> getAssignedProjectsByUserId(int userId) {
        String sql = BASE_PROJECT_SQL + """
                JOIN
                    project_users pu ON p.project_id = pu.project_id
                WHERE
                    pu.user_id = ? AND p.owner_id != ?
                        AND p.parent_project_id IS NULL
                """;

        return jdbcTemplate.query(sql, getProjectRowMapper(), userId, userId);
    }

    public Project getProject(int projectId) {
        String sql = BASE_PROJECT_SQL + """
                WHERE p.project_id = ?
                """;
        List<Project> results = jdbcTemplate.query(sql, getProjectRowMapper(), projectId);
        return results.isEmpty() ? null : results.getFirst();
    }

    public List<Project> getDirectSubProjects(int parentProjectId) {
        String sql = BASE_PROJECT_SQL + """
                WHERE p.parent_project_id = ?
                """;

        return jdbcTemplate.query(sql, getProjectRowMapper(), parentProjectId);
    }

    @Transactional
    public int createProject(Project project) {
        String sql = "INSERT INTO project (owner_id, parent_project_id, title, description, start_date, end_date) VALUES (?,?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, project.getOwnerId());
                    ps.setObject(2, project.getParentProjectId(), Types.INTEGER);
                    ps.setString(3, project.getTitle());
                    ps.setString(4, project.getDescription());
                    ps.setDate(5, Date.valueOf(project.getStartDate()));
                    ps.setDate(6, Date.valueOf(project.getEndDate()));
                    return ps;
                },
                keyHolder
        );
        Number projectId = keyHolder.getKey();

        String sql2 = "INSERT INTO project_users(project_id, user_id, role) VALUES(?,?,?)";

        jdbcTemplate.update(sql2, projectId, project.getOwnerId(), "OWNER");

        return (projectId != null) ? projectId.intValue() : -1;
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

    public int deleteProject(int projectId){
        String sql = "DELETE FROM project WHERE project_id = ?";

        return jdbcTemplate.update(sql, projectId);
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
