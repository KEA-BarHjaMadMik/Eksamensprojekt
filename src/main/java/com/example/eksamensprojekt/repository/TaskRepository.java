package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.Task;
import com.example.eksamensprojekt.model.TaskStatus;
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
    private static final String BASE_TASK_SQL = """
            SELECT
                t.task_id,
                t.parent_task_id,
                t.project_id,
                t.title,
                t.start_date,
                t.end_date,
                t.description,
                t.estimated_hours,
                COALESCE(te.total_hours, 0) AS actual_hours,
                t.status_id,
                ts.status_name
            FROM task t
            LEFT JOIN (
                SELECT task_id, SUM(hours_worked) AS total_hours
                FROM time_entry
                GROUP BY task_id
            ) te ON t.task_id = te.task_id
            JOIN task_status ts ON t.status_id = ts.status_id
            """;

    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //Gets all parent tasks for a project
    public List<Task> getDirectProjectTasks(int projectId) {
        String sql = BASE_TASK_SQL + "WHERE t.project_id = ? AND t.parent_task_id IS NULL"; /* returns only parent tasks */

        return jdbcTemplate.query(sql, getTaskRowMapper(), projectId);
    }

    public Task getTask(int taskId) {
        String sql = BASE_TASK_SQL + "WHERE t.task_id = ?";

        List<Task> results = jdbcTemplate.query(sql, getTaskRowMapper(), taskId);
        return results.isEmpty() ? null : results.getFirst();
    }

    public List<Task> getSubTasks(int parentId) {
        String sql = BASE_TASK_SQL + "WHERE t.parent_task_id = ?";

        return jdbcTemplate.query(sql, getTaskRowMapper(), parentId);
    }

    public List<TaskStatus> getAllTaskStatuses() {
        String sql = """
                SELECT ts.status_id, ts.status_name
                FROM task_status ts
                """;

        return jdbcTemplate.query(sql, getTaskStatusRowMapper());
    }

    public void createTask(Task task) {
        String sql = "INSERT INTO task (parent_task_id, project_id, title, start_date, end_date, description, estimated_hours, status_id) VALUES (?,?,?,?,?,?,?,?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);

            //handles parent_task_id, if null means its the parent task
            ps.setObject(1, task.getParentTaskId(), Types.INTEGER);

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
            ps.setInt(8, 1); // status default to 1

            return ps;
        });
    }

    public void updateTask(Task task) {
        String sql = """
                UPDATE task
                SET
                    parent_task_id = ?,
                    project_id = ?,
                    title = ?,
                    start_date = ?,
                    end_date = ?,
                    description = ?,
                    estimated_hours = ?,
                    status_id = ?
                WHERE task_id = ?
                """;

        jdbcTemplate.update(sql,
                task.getParentTaskId(),
                task.getProjectId(),
                task.getTitle(),
                Date.valueOf(task.getStartDate()),
                Date.valueOf(task.getEndDate()),
                task.getDescription(),
                task.getEstimatedHours(),
                task.getStatus().getStatusId(),
                task.getTaskId()
        );
    }

    public int deleteTask(int taskId){
        String sql = "DELETE FROM task WHERE task_id = ?";

        return jdbcTemplate.update(sql, taskId);
    }

    private RowMapper<Task> getTaskRowMapper() {
        return ((rs, rowNum) -> {
            Date startDate = rs.getDate("start_date");
            Date endDate = rs.getDate("end_date");

            return new Task(
                    rs.getInt("task_id"),
                    rs.getObject("parent_task_id", Integer.class),
                    rs.getInt("project_id"),
                    rs.getString("title"),
                    startDate != null ? startDate.toLocalDate() : null,
                    endDate != null ? endDate.toLocalDate() : null,
                    rs.getString("description"),
                    rs.getDouble("estimated_hours"),
                    rs.getDouble("actual_hours"),
                    new TaskStatus(
                            rs.getInt("status_id"),
                            rs.getString("status_name")
                    ),
                    new ArrayList<>()
            );
        });
    }

    private RowMapper<TaskStatus> getTaskStatusRowMapper() {
        return ((rs, rowNum) -> new TaskStatus(
                rs.getInt("status_id"),
                rs.getString("status_name")
        ));
    }
}
