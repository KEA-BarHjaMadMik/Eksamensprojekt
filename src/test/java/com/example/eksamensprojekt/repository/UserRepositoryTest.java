package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.User;
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
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldGetUserByProjectId() {
        int projectId = 1;

        List<User> userList = userRepository.getUsersByProjectId(projectId);
        assertThat(userList).isNotNull();
        assertThat(userList.size()).isEqualTo(3);
    }

    @Test
    void shouldGetNoUsersByProjectId(){
        int projectId = 999999;

        List<User> userList = userRepository.getUsersByProjectId(projectId);
        assertThat(userList).isNotNull();
        assertThat(userList).isEmpty();
    }

    @Test
    void shouldGetUserByEmail(){
        String email = "anna@example.dk";

        User anna = userRepository.getUserByEmail(email);
        assertThat(anna).isNotNull();
        assertThat(anna.getUserId()).isEqualTo(1);
        assertThat(anna.getName()).isEqualTo("Anna Jensen");
    }

    @Test
    void shouldGetNullUserByEmail(){
        String email = "blank@test.dk";

        User blank = userRepository.getUserByEmail(email);
        assertThat(blank).isNull();
    }

    @Test
    void shouldGetUserById(){
        int userId = 1;

        User anna = userRepository.getUserByUserId(userId);
        assertThat(anna).isNotNull();
        assertThat(anna.getEmail()).isEqualTo("anna@example.dk");
        assertThat(anna.getName()).isEqualTo("Anna Jensen");
    }

    @Test
    void shouldGetNullUserById(){
        int userId = 999999;

        User blank = userRepository.getUserByUserId(userId);
        assertThat(blank).isNull();
    }
}
