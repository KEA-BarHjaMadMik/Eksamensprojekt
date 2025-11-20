package com.example.eksamensprojekt.service;

import com.example.eksamensprojekt.model.User;
import com.example.eksamensprojekt.repository.UserRepository;
import com.example.eksamensprojekt.utils.FormatUtil;
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
        if (user != null && user.getPasswordHash().equals(pw)) {
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
        return repository.registerUser(user);
    }

    public boolean updateUser(User updatedUser) {
        return repository.updateUser(updatedUser);
    }

    public boolean changePassword(int userID, String newPassword) {
        return repository.changePassword(userID, newPassword);
    }

    public boolean deleteUser(int userID) {
        return repository.deleteUser(userID);
    }
}


