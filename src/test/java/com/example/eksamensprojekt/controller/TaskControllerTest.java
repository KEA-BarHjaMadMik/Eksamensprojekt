package com.example.eksamensprojekt.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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
    }

    @Test
    void shouldShowTask() throws Exception {
        Task shownTask = new Task();
        shownTask.setTaskId(2);
        shownTask.setProjectId(1);
        shownTask.setTitle("Show Test");
        shownTask.setStartDate(LocalDate.of(2025, 10, 10));
        shownTask.setEndDate(LocalDate.of(2025, 11, 11));
        shownTask.setEstimatedHours(3.6);
        shownTask.setActualHours(3.5);
        shownTask.setStatus("Done");
        shownTask.setSubTasks(new ArrayList<>());

        when(projectService.hasAccessToProject(1, 1)).thenReturn(true);
        when(taskService.getTask(2)).thenReturn(shownTask);
        when(taskService.getTaskWithTree(2)).thenReturn(shownTask);
        when(projectService.getUserRole(1, 1)).thenReturn("OWNER");

        mockMvc.perform(get("/tasks/2").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("task"))
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attributeExists("userRole"));

        verify(projectService).hasAccessToProject(1, 1);
        verify(taskService).getTaskWithTree(2);
    }

    @Test
    void shouldCreateTaskSuccessfully() throws Exception {
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
    void shouldCreateSubTaskSuccessfully() throws Exception {
        when(projectService.hasAccessToProject(1, 1)).thenReturn(true);

        mockMvc.perform(post("/tasks/create")
                        .session(session)
                        .param("projectId", "1")
                        .param("parentTaskId", "1")
                        .param("title", "Test subtask")
                        .param("description", "Test subtask description")
                        .param("startDate", "2025-11-12")
                        .param("endDate", "2025-11-13")
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

    @Test
    void shouldDenyAccessIfUserIsNotAssignedToProject() throws Exception {
        Task shownTask = new Task();
        shownTask.setTaskId(1);
        shownTask.setProjectId(1);
        shownTask.setTitle("Show Test");
        shownTask.setStartDate(LocalDate.of(2025, 10, 10));
        shownTask.setEndDate(LocalDate.of(2025, 11, 11));
        shownTask.setEstimatedHours(3.6);
        shownTask.setActualHours(3.5);
        shownTask.setStatus("Done");
        shownTask.setSubTasks(new ArrayList<>());

        when(taskService.getTask(1)).thenReturn(shownTask);
        when(projectService.hasAccessToProject(1, 1)).thenReturn(false);

        mockMvc.perform(get("/tasks/1").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        verify(projectService).hasAccessToProject(1, 1);
        verify(projectService, never()).getProjectWithTree(1);
        verify(taskService, never()).getTaskWithTree(1);
    }
    //Create task test success/failed
    //Create subtask test
    //Show task when has access test
    //Show task when not has access test


}
