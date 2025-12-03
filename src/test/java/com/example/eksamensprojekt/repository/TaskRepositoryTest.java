package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

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
}
