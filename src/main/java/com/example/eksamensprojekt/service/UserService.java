package com.example.eksamensprojekt.service;

import com.example.eksamensprojekt.model.User;
import com.example.eksamensprojekt.repository.UserRepository;
import com.example.eksamensprojekt.utils.PasswordUtil;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User authenticate(String email, String pw) {
        // Try to retrieve the username by username or email
        User user = repository.getUserByEmail(email);

        // Check if user exists and password matches
        if (user != null && PasswordUtil.checkPassword(pw, user.getPasswordHash())) {
            // Authentication successful — return the full User object
            return user;
        }

        // Authentication failed — return null
        return null;
    }

    public User getUserByUserID(int userID){
        return repository.getUserByUserID(userID);
    }

    public boolean emailExists(String email) {
        return repository.emailExists(email);
    }

    public boolean registerUser(User user) {
        user.setPasswordHash(PasswordUtil.hashPassword(user.getPasswordHash()));
        return repository.registerUser(user);
    }

    public boolean updateUser(User updatedUser) {
        return repository.updateUser(updatedUser);
    }

    public boolean changePassword(int userID, String newPassword) {
        String passwordHash = PasswordUtil.hashPassword(newPassword);
        return repository.changePassword(userID, passwordHash);
    }

    public boolean deleteUser(int userID) {
        return repository.deleteUser(userID);
    }
}


