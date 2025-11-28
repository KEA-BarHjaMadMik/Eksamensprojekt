package com.example.eksamensprojekt.controller;

import com.example.eksamensprojekt.model.User;
import com.example.eksamensprojekt.service.UserService;
import com.example.eksamensprojekt.utils.SessionUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm(HttpSession session) {
        // if already logged in, return to front page, else proceed to form
        return SessionUtil.isLoggedIn(session) ? "redirect:/" : "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email, @RequestParam("pw") String pw,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        // Attempt to authenticate the user
        User user = userService.authenticate(email, pw);

        if (user != null) {
            // Login successful — store the username in the session
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userEmail", user.getEmail());
            // redirect
            return "redirect:/";
        }

        // Login failed — add an attribute to indicate incorrect credentials
        redirectAttributes.addFlashAttribute("wrongCredentials", true);

        // Return to the login page
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // invalidate session and return landing page
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/register_user")
    public String showRegistrationForm(HttpSession session, Model model) {
        // if already logged in, return to front page, else proceed to form
        if (SessionUtil.isLoggedIn(session)) {
            return "index";
        }

        model.addAttribute("newUser", new User());
        return "user_registration_form";
    }


    @PostMapping("/register_user")
    public String registerUser(@Valid @ModelAttribute User newUser,
                               BindingResult bindingResult,
                               @RequestParam("confirmPassword") String confirmPassword,
                               Model model) {

        // Check for field validation errors
        boolean fieldsHaveErrors = bindingResult.hasErrors();

        // Check if email is free
        boolean emailTaken = userService.emailExists(newUser.getEmail());
        if (emailTaken) {
            model.addAttribute("emailTaken", true);
        }

        // Check if passwords match
        boolean passwordMismatch = !newUser.getPasswordHash().equals(confirmPassword);
        if (passwordMismatch) {
            model.addAttribute("passwordMismatch", true);
        }

        // If validation failed, return to form
        if (fieldsHaveErrors || emailTaken || passwordMismatch) {
            model.addAttribute("newUser", newUser);
            return "user_registration_form";
        }

        // Proceed with saving the user
        userService.registerUser(newUser);
        return "redirect:/login";
    }

    @GetMapping("/user_admin")
    public String showUserAdminPage(HttpSession session, Model model) {
        // Ensure user is logged in
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }

        // Retrieve userId from session
        int userId = (int) session.getAttribute("userId");

        // Fetch full user object
        User user = userService.getUserByUserId(userId);

        // Add user to model
        model.addAttribute("user", user);

        return "user_admin";
    }

    @PostMapping("/update_user")
    public String updateUser(@Valid @ModelAttribute("user") User user,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model) {
        // Ensure user is logged in
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }

        // Check for field validation errors
        boolean fieldsHaveErrors = bindingResult.hasErrors();

        // Check if the new email is taken (and not the current one)
        boolean emailTaken = userService.emailExists(user.getEmail()) &&
                !userService.getUserByUserId(user.getUserId()).getEmail().equals(user.getEmail());
        if (emailTaken) {
            model.addAttribute("emailTaken", true);
        }

        // If validation failed, return to form
        if (fieldsHaveErrors || emailTaken) {
            return "user_admin";
        }

        // Proceed with updating the user
        if (userService.updateUser(user)) {
            model.addAttribute("updateSuccess", true);
        } else {
            model.addAttribute("updateFailure", true);
        }

        return "redirect:/user_admin";
    }

    @GetMapping("/change_password")
    public String showChangePasswordForm(HttpSession session) {
        // Ensure user is logged in
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }

        return "change_password";
    }

    @PostMapping("/change_password")
    public String changePassword(@RequestParam("password") String password,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmNewPassword") String confirmNewPassword,
                                 HttpSession session,
                                 Model model) {

        // Retrieve userId from session
        int userId = (int) session.getAttribute("userId");
        // Retrieve user email
        String email = userService.getUserByUserId(userId).getEmail();

        // Attempt to authenticate the user
        User user = userService.authenticate(email, password);

        boolean incorrectPassword = user == null;
        if (incorrectPassword) {
            model.addAttribute("incorrectPassword", true);
        }

        // TO-DO magic number removal
        boolean passwordTooShort = newPassword.length() < 6;
        if(passwordTooShort) {
            model.addAttribute("passwordTooShort", true);
        }

        // Check if passwords match
        boolean passwordMismatch = !newPassword.equals(confirmNewPassword);
        if (passwordMismatch) {
            model.addAttribute("passwordMismatch", true);
        }

        // If validation failed, return to form
        if (incorrectPassword || passwordTooShort || passwordMismatch) {
            return "change_password";
        }

        // Proceed with updating the password
        if (userService.changePassword(userId, newPassword)) {
            return "redirect:/user_admin";
        } else {
            model.addAttribute("updateFailure", true);
            return "change_password";
        }
    }

    @PostMapping("/delete")
    public String deleteUser(HttpSession session, Model model){
        // Ensure user is logged in
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }

        // Retrieve userId from session
        int userId = (int) session.getAttribute("userId");

        // Proceed with deleting user
        if(userService.deleteUser(userId)) {
            session.invalidate();
            return "redirect:/";
        }else{
            model.addAttribute("deleteFailure", true);
            return "user_admin";
        }
    }
}

