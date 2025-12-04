package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void shouldReturn0CountByEmail(){
        //This test checks if an email is already taken.
        //If count is 0 then there are no registered emails in the database, so account can be created with that email

        String email = "new@example.dk";

        int emailTaken = userRepository.countByEmail(email);
        assertThat(emailTaken).isEqualTo(0);
    }

    @Test
    void shouldReturn1CountByEmail(){
        //This test checks if an email is already taken.
        //If count is 1 or more then there is already an email in the database with that signature,
        //so account cannot be created

        String email = "anna@example.dk";

        int emailTaken = userRepository.countByEmail(email);
        assertThat(emailTaken).isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldUpdateUser(){
        int userId = 1;
        String newTitle = "Testleder";
        User user = userRepository.getUserByUserId(userId);
        user.setTitle(newTitle);

        int affectedRows = userRepository.updateUser(user);
        assertThat(affectedRows).isEqualTo(1);

        User updatedUser = userRepository.getUserByUserId(userId);
        assertEquals(newTitle, updatedUser.getTitle());
    }

    @Test
    void shouldChangePassword(){
        int userId = 1;
        String newPasswordHash = "$2a$10$tzNpfmbuFlp4q01pT3Ir1OkhfzxqFlPzkykHaXi5kZK0FeG7rTbfC";

        int affectedRows = userRepository.changePassword(userId, newPasswordHash);
        assertThat(affectedRows).isEqualTo(1);

        User updatedUser = userRepository.getUserByUserId(userId);
        assertEquals(newPasswordHash, updatedUser.getPasswordHash());
    }
}
