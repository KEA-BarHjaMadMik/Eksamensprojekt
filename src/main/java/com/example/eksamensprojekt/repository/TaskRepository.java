package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.Task;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TaskRepository {
    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //Gets all parent tasks for a project
    public List<Task> getDirectProjectTasks(int projectId) {
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
                WHERE t.project_id = ? AND t.parent_task_id IS NULL /* returns only parent tasks */
                GROUP BY t.task_id
                """;
        return jdbcTemplate.query(sql, getTaskRowMapper(), projectId);
    }

    public Task getTask(int taskId) {
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
                WHERE t.task_id = ?
                GROUP BY t.task_id
                """;

        List<Task> results = jdbcTemplate.query(sql, getTaskRowMapper(), taskId);
        return results.isEmpty() ? null : results.getFirst();
    }

    public List<Task> getSubTasks(int parentId) {
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

        return jdbcTemplate.query(sql, getTaskRowMapper(), parentId);
    }

    public boolean createTask(Task task) {
        String sql = "INSERT INTO task (parent_task_id, project_id, title, start_date, end_date, description, estimated_hours, status_id) VALUES (?,?,?,?,?,?,?,?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);

            //handles parent_task_id, if null means its the parent task
            ps.setObject(1, task.getParentTaskId(), Types.INTEGER);
//            int parentTaskId = task.getParentTaskId();
//            if (parentTaskId != 0) {
//                ps.setInt(1, parentTaskId);
//            } else {
//                ps.setNull(1, Types.INTEGER);
//            }

            //required fields
            ps.setInt(2, task.getProjectId());
            ps.setString(3, task.getTitle());

            //handles start_Date
            LocalDate startDate = task.getStartDate();
            if (startDate != null) {
                ps.setDate(4, Date.valueOf(startDate));
            } else {
                ps.setNull(4, Types.DATE);
            }
            //handles end_Date
            LocalDate endDate = task.getEndDate();
            if (endDate != null) {
                ps.setDate(5, Date.valueOf(endDate));
            } else {
                ps.setNull(5, Types.DATE);
            }

            //Handles description
            String description = task.getDescription();
            if (description != null) {
                ps.setString(6, description);
            } else {
                ps.setNull(6, Types.VARCHAR);
            }

            ps.setDouble(7, task.getEstimatedHours());
            ps.setInt(8, 1);

            return ps;
        });
        return true;
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
