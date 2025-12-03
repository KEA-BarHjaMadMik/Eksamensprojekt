package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:h2init.sql", executionPhase = BEFORE_TEST_METHOD)
public class TaskRepositoryTest {
    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldGetDirectProjectTasks(){
        int projectId = 1;
        List<Task> tasks = taskRepository.getDirectProjectTasks(projectId);

        assertThat(tasks).isNotNull();

        assertThat(tasks).isNotEmpty();

        assertThat(tasks.size()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void shouldGetNoProjectTasks(){
        int projectId = 0;
        List<Task> tasks = taskRepository.getDirectProjectTasks(projectId);

        assertThat(tasks).isNotNull();

        assertThat(tasks).isEmpty();
    }

    @Test
    void shouldGetTask(){
        int taskId = 1;
        Task task = taskRepository.getTask(taskId);

        assertThat(task).isNotNull();
        assertThat(task.getTitle()).isEqualTo("Design Mockups");
        assertThat(task.getParentTaskId()).isNull();
    }

    @Test
    void shouldGetNullTask(){
        int taskId = 999999;
        Task task = taskRepository.getTask(taskId);

        assertThat(task).isNull();
    }

    @Test
    void shouldGetSubTasks(){
        int parentTaskId = 1;
        List<Task> subTasks = taskRepository.getSubTasks(parentTaskId);

        assertThat(subTasks).isNotNull();
        assertThat(subTasks).isNotEmpty();
        assertThat(subTasks.size()).isEqualTo(2);
    }

    @Test
    void shouldGetNoSubTasks(){
        int parentTaskId = 999999;
        List<Task> subTasks = taskRepository.getSubTasks(parentTaskId);

        assertThat(subTasks).isNotNull();
        assertThat(subTasks).isEmpty();
    }

    @Test
    void shouldCreateTask(){
        int projectId = 1;
        String title = "Test task";
        LocalDate startDate = LocalDate.of(2026,1,1);
        LocalDate endDate = LocalDate.of(2026,2,1);
        String description = "Testing";
        double estimatedHours = 1;

        Task testTask = new Task();
        testTask.setProjectId(projectId);
        testTask.setTitle(title);
        testTask.setStartDate(startDate);
        testTask.setEndDate(endDate);
        testTask.setDescription(description);
        testTask.setEstimatedHours(estimatedHours);

        taskRepository.createTask(testTask);

        Task createdTask = taskRepository.getTask(19); //19 should be the test task id
        assertThat(createdTask).isNotNull();
        assertThat(createdTask.getProjectId()).isEqualTo(1);
        assertThat(createdTask.getTitle()).isEqualTo("Test task");
        assertThat(createdTask.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(createdTask.getEndDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(createdTask.getDescription()).isEqualTo("Testing");
        assertThat(createdTask.getEstimatedHours()).isEqualTo(1);
    }
}
