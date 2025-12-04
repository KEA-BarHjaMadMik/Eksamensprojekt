package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.model.ProjectRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:h2init.sql", executionPhase = BEFORE_TEST_METHOD)
class ProjectRepositoryTest {
    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void shouldGetProjectsByOwnerId(){
        int ownerId = 1;
        List<Project> projects = projectRepository.getProjectsByOwnerId(ownerId);

        assertThat(projects).isNotNull();
        assertThat(projects).isNotEmpty();
        assertThat(projects.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldNotGetProjectsByOwnerId(){
        int ownerId = 999999;
        List<Project> projects = projectRepository.getProjectsByOwnerId(ownerId);

        assertThat(projects).isNotNull();
        assertThat(projects).isEmpty();
    }

    @Test
    void shouldGetAssignedProjectsByUserId(){
        int userId = 2;
        List<Project> assignedProjects = projectRepository.getAssignedProjectsByUserId(userId);

        assertThat(assignedProjects).isNotNull();
        assertThat(assignedProjects).isNotEmpty();
        assertThat(assignedProjects.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldGetNoAssignedProjectsByUserId(){
        int userId = 999999;
        List<Project> assignedProjects = projectRepository.getAssignedProjectsByUserId(userId);

        assertThat(assignedProjects).isNotNull();
        assertThat(assignedProjects).isEmpty();
    }

    @Test
    void shouldGetProject(){
        int projectId = 1;
        Project project = projectRepository.getProject(projectId);

        assertThat(project).isNotNull();
        assertThat(project.getTitle()).isEqualTo("Website Redesign");
        assertThat(project.getOwnerId()).isEqualTo(1);
    }

    @Test
    void shouldGetNullProject(){
        int projectId = 999999;
        Project project = projectRepository.getProject(projectId);

        assertThat(project).isNull();
    }

    @Test
    void shouldGetDirectSubProjects(){
        int parentProjectId = 1;
        List<Project> subProjects = projectRepository.getDirectSubProjects(parentProjectId);

        assertThat(subProjects).isNotNull();
        assertThat(subProjects).isNotEmpty();
        assertThat(subProjects.size()).isEqualTo(1);
    }

    @Test
    void shouldGetNoDirectSubProjects(){
        int parentProjectId = 999999;
        List<Project> subProjects = projectRepository.getDirectSubProjects(parentProjectId);

        assertThat(subProjects).isNotNull();
        assertThat(subProjects).isEmpty();
    }

    @Test
    void shouldCreateProject(){
        int ownerId = 1;
        String title = "Test create";
        String description = "Test description";
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 2, 1);

        Project createdProject = new Project();
        createdProject.setOwnerId(ownerId);
        createdProject.setTitle(title);
        createdProject.setDescription(description);
        createdProject.setStartDate(startDate);
        createdProject.setEndDate(endDate);

        int projectId = projectRepository.createProject(createdProject);
        assertThat(projectId).isEqualTo(6);

        Project project = projectRepository.getProject(projectId);

        assertThat(project.getOwnerId()).isEqualTo(ownerId);
        assertThat(project.getTitle()).isEqualTo("Test create");
        assertThat(project.getDescription()).isEqualTo("Test description");
        assertThat(project.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(project.getEndDate()).isEqualTo(LocalDate.of(2026, 2, 1));
    }

    @Test
    void shouldCreateSubProject(){
        int ownerId = 1;
        int parentProjectId = 1;
        String title = "Test create";
        String description = "Test description";
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 2, 1);

        Project createdProject = new Project();
        createdProject.setOwnerId(ownerId);
        createdProject.setParentProjectId(parentProjectId);
        createdProject.setTitle(title);
        createdProject.setDescription(description);
        createdProject.setStartDate(startDate);
        createdProject.setEndDate(endDate);

        int projectId = projectRepository.createProject(createdProject);
        assertThat(projectId).isEqualTo(6);

        Project project = projectRepository.getProject(projectId);

        assertThat(project.getOwnerId()).isEqualTo(ownerId);
        assertThat(project.getParentProjectId()).isEqualTo(1);
        assertThat(project.getTitle()).isEqualTo("Test create");
        assertThat(project.getDescription()).isEqualTo("Test description");
        assertThat(project.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(project.getEndDate()).isEqualTo(LocalDate.of(2026, 2, 1));
    }

    @Test
    void shouldGetProjectUserRole(){
        int projectId = 1;
        int userId = 1;
        ProjectRole userRole = projectRepository.getProjectUserRole(projectId, userId);

        assertThat(userRole).isNotNull();
        assertThat(userRole.getRole()).isEqualTo("OWNER");
    }

    @Test
    void shouldGetNullProjectUserRole(){
        int projectId = 1;
        int userId = 999999;
        ProjectRole userRole = projectRepository.getProjectUserRole(projectId, userId);

        assertThat(userRole).isNull();
    }

    @Test
    void shouldDeleteProject(){
        int projectId = 1;

        int projectDeleted = projectRepository.deleteProject(projectId);

        assertThat(projectDeleted).isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldDeleteNoProject(){
        int projectId = 999999;
        int projectsDeleted = projectRepository.deleteProject(projectId);

        assertThat(projectsDeleted).isEqualTo(0);
    }

    @Test
    void userIsAssignedToProject(){
        int projectId = 1;
        int userId = 2;

        boolean isAssigned = projectRepository.isUserAssignedToProject(projectId, userId);

        assertThat(isAssigned).isEqualTo(true);
    }

    @Test
    void userIsNotAssignedToProject(){
        int projectId = 1;
        int userId = 999999;

        boolean isAssigned = projectRepository.isUserAssignedToProject(projectId, userId);

        assertThat(isAssigned).isEqualTo(false);
    }
}
