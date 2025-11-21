package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.model.Task;
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
                rs.getObject("parent_project_id", Integer.class), // guard against null value
                rs.getString("title"),
                rs.getString("description"),
                rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null,
                rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null,
                new ArrayList<>(),
                new ArrayList<>())
        );
    }

    public List<Task> getDirectProjectTasks(int projectID) {
        String sql = """
                SELECT
                    t.task_id,
                    t.parent_task_id,
                    t.project_id,
                    t.title,
                    t.start_date,
                    t.end_date,
                    t.description,
                    t.estimated_hours,
                    COALESCE(SUM(te.hours_worked), 0) AS actual_hours,
                    ts.status_name
                FROM task t
                LEFT JOIN time_entry te ON t.task_id = te.task_id
                JOIN task_status ts ON t.status_id = ts.status_id
                WHERE t.project_id = ? AND t.parent_task_id IS NULL
                GROUP BY t.task_id
                """;

        return jdbcTemplate.query(sql, getTaskRowMapper(), projectID);
    }

    public List<Task> getSubTasks(int parentID) {
        String sql = """
                SELECT
                    t.task_id,
                    t.parent_task_id,
                    t.project_id,
                    t.title,
                    t.start_date,
                    t.end_date,
                    t.description,
                    t.estimated_hours,
                    COALESCE(SUM(te.hours_worked), 0) AS actual_hours,
                    ts.status_name
                FROM task t
                LEFT JOIN time_entry te ON t.task_id = te.task_id
                JOIN task_status ts ON t.status_id = ts.status_id
                WHERE t.parent_task_id = ?
                GROUP BY t.task_id
                """;

        return jdbcTemplate.query(sql, getTaskRowMapper(), parentID);
    }

    private RowMapper<Task> getTaskRowMapper() {
        return ((rs, rowNum) -> new Task(
                rs.getInt("task_id"),
                rs.getObject("parent_task_id", Integer.class),
                rs.getInt("project_id"),
                rs.getString("title"),
                rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null,
                rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null,
                rs.getString("description"),
                rs.getDouble("estimated_hours"),
                rs.getDouble("actual_hours"),
                rs.getString("status_name"),
                new ArrayList<>()
        ));
    }
}
