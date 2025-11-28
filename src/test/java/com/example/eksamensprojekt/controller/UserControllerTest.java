package com.example.eksamensprojekt.controller;

import com.example.eksamensprojekt.model.User;
import com.example.eksamensprojekt.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.FlashMap;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldShowLoginFormWhenNotLoggedIn() throws Exception {
        // arrange


        // act and assert
        mockMvc.perform(get("/login")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void shouldRedirectWhenLoggedIn() throws Exception {
        // arrange
        session.setAttribute("userId", 1);

        // act and assert
        mockMvc.perform(get("/login")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void shouldLogin() throws Exception {
        // arrange
        User user = new User();
        user.setUserId(1);
        user.setEmail("test@email.dk");

        when(userService.authenticate("test@email.dk", "test123")).thenReturn(user);

        // act
        mockMvc.perform(post("/login")
                        .session(session)
                        .param("email", "test@email.dk")
                        .param("pw", "test123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(userService, times(1)).authenticate("test@email.dk","test123");

        assertEquals(1, session.getAttribute("userId"));
        assertEquals("test@email.dk", session.getAttribute("userEmail"));
    }

    @Test
    void shouldFailLoginWhenCredentialsIncorrect() throws Exception {
        // arrange
        when(userService.authenticate("test@email.dk", "incorrectPassword")).thenReturn(null);

        // act
        mockMvc.perform(post("/login")
                        .session(session)
                        .param("email", "test@email.dk")
                        .param("pw", "incorrectPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("wrongCredentials", true));

        verify(userService, times(1)).authenticate("test@email.dk","incorrectPassword");

        assertNull(session.getAttribute("userId"));
        assertNull(session.getAttribute("userEmail"));
    }

    @Test
    void loginPageReceivesWrongCredentialsFlash() throws Exception {
        // arrange
        when(userService.authenticate("test@email.dk", "incorrectPassword")).thenReturn(null);

        // perform the POST and capture flash attributes
        MvcResult postResult = mockMvc.perform(post("/login")
                        .param("email", "test@email.dk")
                        .param("pw", "incorrectPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andReturn();

        FlashMap flashMap = postResult.getFlashMap();

        // perform the GET with flash attributes
        MvcResult getResult = mockMvc.perform(get("/login").flashAttrs(flashMap))
                .andExpect(status().isOk())
                .andReturn();

        // assert the login page received the attribute
        assertEquals(true, getResult.getModelAndView().getModel().get("wrongCredentials"));
    }

    @Test
    void shouldLogoutAndInvalidateSession() throws Exception {
        // arrange
        session.setAttribute("userId", 1);
        session.setAttribute("userEmail", "test@email.dk");

        // act
        mockMvc.perform(get("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // assert
        assertThrows(IllegalStateException.class, () -> session.getAttribute("userId"));
    }

    @Test
    void shouldShowRegistrationFormWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/register_user").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("user_registration_form"))
                .andExpect(model().attributeExists("newUser"))
                .andExpect(model().attribute("newUser", instanceOf(User.class)))
                .andExpect(model().attribute("newUser", notNullValue()));
    }

    @Test
    void shouldRedirectToFrontPageWhenLoggedIn() throws Exception {
        // arrange
        session.setAttribute("userId", 1);

        mockMvc.perform(get("/register_user").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void shouldRegisterUserWhenValid() throws Exception {
        // userService.emailExists returns false
        when(userService.emailExists("test@mail.dk")).thenReturn(false);

        mockMvc.perform(post("/register_user")
                        .param("email", "test@mail.dk")
                        .param("passwordHash", "test123")
                        .param("confirmPassword", "test123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).registerUser(any(User.class));
    }

    @Test
    void shouldReturnFormWhenEmailIsTaken() throws Exception {
        when(userService.emailExists("used@mail.dk")).thenReturn(true);

        mockMvc.perform(post("/register_user")
                        .param("email", "used@mail.dk")
                        .param("passwordHash", "test123")
                        .param("confirmPassword", "test123"))
                .andExpect(status().isOk())
                .andExpect(view().name("user_registration_form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("newUser", "email"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    void shouldReturnFormWhenPasswordsDoNotMatch() throws Exception {
        when(userService.emailExists("test@mail.dk")).thenReturn(false);

        mockMvc.perform(post("/register_user")
                        .param("email", "test@mail.dk")
                        .param("passwordHash", "pw1")
                        .param("confirmPassword", "pw2"))
                .andExpect(status().isOk())
                .andExpect(view().name("user_registration_form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("newUser", "passwordHash"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    void shouldReturnFormWhenBeanValidationFails() throws Exception {

        mockMvc.perform(post("/register_user")
                        .param("email", "")                       // invalid
                        .param("passwordHash", "pw")
                        .param("confirmPassword", "pw"))
                .andExpect(status().isOk())
                .andExpect(view().name("user_registration_form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("newUser", "email"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    void showUserAdminPage() {
    }

    @Test
    void updateUser() {
    }

    @Test
    void showChangePasswordForm() {
    }

    @Test
    void changePassword() {
    }

    @Test
    void deleteUser() {
    }


    //CreateUser test success/failed?
    //Already used email test?


}
