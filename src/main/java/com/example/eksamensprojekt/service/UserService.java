package com.example.eksamensprojekt.service;

import com.example.eksamensprojekt.exceptions.DatabaseOperationException;
import com.example.eksamensprojekt.exceptions.UserNotFoundException;
import com.example.eksamensprojekt.model.User;
import com.example.eksamensprojekt.repository.UserRepository;
import com.example.eksamensprojekt.utils.PasswordUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String email, String pw) {
        // Try to retrieve the username by username or email
        try {
            User user = userRepository.getUserByEmail(email);

            // Check if user exists and password matches
            if (user != null && PasswordUtil.checkPassword(pw, user.getPasswordHash())) {
                // Authentication successful — return the full User object
                return user;
            }

            // Incorrect email or password — return null
            return null;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to authenticate user", e);
        }
    }

    public User getUserByUserID(int userID) {
        try {
            User user = userRepository.getUserByUserID(userID);
            if (user == null) throw new UserNotFoundException(userID);
            return user;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve user", e);
        }
    }

    public boolean emailExists(String email) {
        try {
            return userRepository.countByEmail(email) > 0;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve emails from database", e);
        }
    }

    public void registerUser(User user) {
        try {
            String plainPassword = user.getPasswordHash();
            String passwordHash = PasswordUtil.hashPassword(plainPassword);
            user.setPasswordHash(passwordHash);
            userRepository.registerUser(user);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to register user", e);
        }
    }

    public boolean updateUser(User updatedUser) {
        try {
            int rowsAffected = userRepository.updateUser(updatedUser);
            if (rowsAffected == 0) throw new UserNotFoundException(updatedUser.getUserID());
            return true; // User updated
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to update user", e);
        }
    }

    public boolean changePassword(int userID, String newPassword) {
        try {
            String passwordHash = PasswordUtil.hashPassword(newPassword);
            int rowsAffected = userRepository.changePassword(userID, passwordHash);
            if (rowsAffected == 0) throw new UserNotFoundException(userID);
            return true; // Password updated
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to update password", e);
        }
    }

    public boolean deleteUser(int userID) {
        try {
            int rowsAffected = userRepository.deleteUser(userID);
            if (rowsAffected == 0) throw new UserNotFoundException(userID);
            return true; // User deleted
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to delete user", e);
        }
    }
}


