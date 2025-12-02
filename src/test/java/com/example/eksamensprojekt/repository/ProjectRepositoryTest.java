package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.model.ProjectRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

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
        int ownerId = 0;
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
    void shouldNotGetAssignedProjectsByUserId(){
        int userId = 0;
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
        int projectId = 0;
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
    void shouldNotGetNoSubProjects(){
        int parentProjectId = 0;
        List<Project> subProjects = projectRepository.getDirectSubProjects(parentProjectId);

        assertThat(subProjects).isNotNull();
        assertThat(subProjects).isEmpty();
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
        int userId = 0;
        ProjectRole userRole = projectRepository.getProjectUserRole(projectId, userId);

        assertThat(userRole).isNull();
    }
}
