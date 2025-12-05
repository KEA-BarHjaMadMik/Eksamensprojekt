package com.example.eksamensprojekt.controller;

import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.service.ProjectService;
import com.example.eksamensprojekt.service.UserService;
import com.example.eksamensprojekt.utils.SessionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ProjectController.class)
public class ProjectControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private UserService userService;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute("userId", 1);
    }

    @Test
    void shouldShowProjectsWhenLoggedIn() throws Exception {
        // Arrange mock data
        List<Project> ownedProjects = List.of(new Project());
        List<Project> assignedProjects = List.of();

        //when calling getProjectByOwnerId(1) return ownedProjects
        when(projectService.getProjectsByOwnerId(1)).thenReturn(ownedProjects);
        when(projectService.getAssignedProjectsByUserId(1)).thenReturn(assignedProjects);

        // Act & Assert
        mockMvc.perform(get("/projects").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("projects"))
                .andExpect(model().attributeExists("projects"))
                .andExpect(model().attributeExists("assignedProjects"));

        // Verify
        verify(projectService).getProjectsByOwnerId(1);
        verify(projectService).getAssignedProjectsByUserId(1);
    }

    @Test
    void shouldRedirectToLoginWhenNotLoggedIn() throws Exception {
        // Arrange empty session
        MockHttpSession emptySession = new MockHttpSession();

        // Act & Assert
        mockMvc.perform(get("/projects").session(emptySession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // Verify service was NOT called
        verifyNoInteractions(projectService);
    }

    @Test
    void shouldCreateProjectSuccessfully() throws Exception {
        // Arrange mock service call
        when(projectService.createProject(any(Project.class), anyBoolean(), anyInt())).thenReturn(1);

        // Act & Assert
        mockMvc.perform(post("/projects/create")
                        .session(session)
                        .param("ownerId", "1")
                        .param("title", "Test Project")
                        .param("description", "Test Description")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1"));

        // Verify service was called with correct data
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        ArgumentCaptor<Boolean> booleanCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(projectService).createProject(
                projectCaptor.capture(),
                booleanCaptor.capture(),
                intCaptor.capture()
        );

        Project captured = projectCaptor.getValue();
        assertThat(captured.getTitle()).isEqualTo("Test Project");
        assertThat(captured.getDescription()).isEqualTo("Test Description");

        // assert default values
        assertThat(booleanCaptor.getValue()).isFalse(); // copyTeam defaults to false
        assertThat(intCaptor.getValue()).isEqualTo(SessionUtil.getCurrentUserId(session));   // matches session userId
    }

    @Test
    void shouldCreateSubProjectSuccessfully() throws Exception {
        // Arrange mock service call
        when(projectService.createProject(any(Project.class), anyBoolean(), anyInt())).thenReturn(6);

        // Act & Assert
        mockMvc.perform(post("/projects/create")
                        .session(session)
                        .param("ownerId", "1")
                        .param("parentProjectId", "1") // project has parent id
                        .param("title", "Test Subproject")
                        .param("description", "Test Description")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/6"));

        // Verify service was called with correct arguments
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        ArgumentCaptor<Boolean> copyTeamCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Integer> creatorIdCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(projectService).createProject(
                projectCaptor.capture(),
                copyTeamCaptor.capture(),
                creatorIdCaptor.capture()
        );

        Project captured = projectCaptor.getValue();
        assertThat(captured.getParentProjectId()).isEqualTo(1);
        assertThat(captured.getTitle()).isEqualTo("Test Subproject");

        // assert default parameters passed correctly
        assertThat(copyTeamCaptor.getValue()).isFalse(); // defaults to false in controller if not provided
        assertThat(creatorIdCaptor.getValue()).isEqualTo(SessionUtil.getCurrentUserId(session)); // matches session userId
    }

    @Test
    void shouldDenyAccessWhenUserNotAssignedToProject() throws Exception {
        // Arrange mock service call
        when(projectService.hasAccessToProject(1, 1)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/projects/1").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        // Verify getProjectWithTree was NOT called
        verify(projectService).hasAccessToProject(1, 1);
        verify(projectService, never()).getProjectWithTree(anyInt());
    }
    //create subproject test??
    //view project/subproject test??
    //

}
