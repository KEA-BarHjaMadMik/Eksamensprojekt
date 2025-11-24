package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.Task;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TaskRepository {
    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        return ((rs, rowNum) -> {
            Date startDate = rs.getDate("start_date");
            Date endDate = rs.getDate("end_date");

            return new Task(
                    rs.getInt("task_id"),
                    rs.getInt("parent_task_id"),
                    rs.getInt("project_id"),
                    rs.getString("title"),
                    startDate != null ? startDate.toLocalDate() : null,
                    endDate != null ? endDate.toLocalDate() : null,
                    rs.getString("description"),
                    rs.getDouble("estimated_hours"),
                    rs.getDouble("actual_hours"),
                    rs.getString("status_name"),
                    new ArrayList<>()
            );
        });
    }
}
