package com.example.eksamensprojekt.controller;

import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.model.Task;
import com.example.eksamensprojekt.service.ProjectService;
import com.example.eksamensprojekt.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;


@WebMvcTest(TaskController.class)
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private ProjectService projectService;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute("userId", 1);
        Project project = new Project(1, 1, 0, "Test Project", "Test",
                                      LocalDate.ofEpochDay(2025-1-1),
                                      LocalDate.ofEpochDay(2025-12-12),
                                      new ArrayList<Project>(),
                                      new ArrayList<Task>());
        Task task = new Task(1,
                            0,
                            1,
                            "Test task 1",
                            LocalDate.ofEpochDay(2025-11-11),
                            LocalDate.ofEpochDay(2025-11-12),
                            "Test task setup",
                            6.5,
                            6.0,
                            "Test status",
                            new ArrayList<Task>());
    }

    @Test
    void shouldCreateTask() throws Exception {
        when(projectService.hasAccessToProject(1, 1)).thenReturn(true);

        mockMvc.perform(post("/tasks/create")
                        .session(session)
                        .param("projectId", "1")
                        .param("title", "Test task 2")
                        .param("description", "Test description")
                        .param("startDate", "2025-07-12")
                        .param("endDate", "2025-08-12")
                        .param("estimatedHours", "6.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1"));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).createTask(captor.capture());

        Task captured = captor.getValue();
        assertThat(captured.getTitle()).isEqualTo("Test task 2");
        assertThat(captured.getProjectId()).isEqualTo(1);
        assertThat(captured.getDescription()).isEqualTo("Test description");
        assertThat(captured.getEstimatedHours()).isEqualTo(6.5);
    }

    @Test
    void shouldCreateSubTask() throws Exception {
        when(projectService.hasAccessToProject(1, 1)).thenReturn(true);

        mockMvc.perform(post("/tasks/create")
                        .session(session)
                        .param("projectId", "1")
                        .param("parentTaskId", "1")
                        .param("title", "Test subtask")
                        .param("description", "Test subtask description")
                        .param("startDate", "2025-07-12")
                        .param("endDate", "2025-07-13")
                        .param("estimatedHours", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/1"));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).createTask(captor.capture());

        Task captured = captor.getValue();
        assertThat(captured.getParentTaskId()).isEqualTo(1);
        assertThat(captured.getProjectId()).isEqualTo(1);
        assertThat(captured.getTitle()).isEqualTo("Test subtask");
        assertThat(captured.getDescription()).isEqualTo("Test subtask description");
    }
    //Create task test success/failed
    //Create subtask test
    //Show task when has access test
    //Show task when not has access test


}
