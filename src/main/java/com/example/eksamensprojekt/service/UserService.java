package com.example.eksamensprojekt.service;

import com.example.eksamensprojekt.exceptions.DatabaseOperationException;
import com.example.eksamensprojekt.exceptions.UserNotFoundException;
import com.example.eksamensprojekt.model.User;
import com.example.eksamensprojekt.repository.UserRepository;
import com.example.eksamensprojekt.utils.PasswordUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<User> getUsersByProjectId(int projectId) {
        try {
            return userRepository.getUsersByProjectId(projectId);
        }catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve users by project id=" + projectId, e);
        }
    }

    public User getUserByUserId(int userId) {
        try {
            User user = userRepository.getUserByUserId(userId);
            if (user == null) throw new UserNotFoundException(userId);
            return user;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to retrieve user", e);
        }
    }

    public User getUserByEmail(String email) {
        try {
            User user = userRepository.getUserByEmail(email);
            if(user == null) throw new UserNotFoundException(email);
            return user;
        }catch (DataAccessException e) {
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
            if (rowsAffected == 0) throw new UserNotFoundException(updatedUser.getUserId());
            return true; // User updated
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to update user", e);
        }
    }

    public boolean changePassword(int userId, String newPassword) {
        try {
            String passwordHash = PasswordUtil.hashPassword(newPassword);
            int rowsAffected = userRepository.changePassword(userId, passwordHash);
            if (rowsAffected == 0) throw new UserNotFoundException(userId);
            return true; // Password updated
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to update password", e);
        }
    }

    public boolean deleteUser(int userId) {
        try {
            int rowsAffected = userRepository.deleteUser(userId);
            if (rowsAffected == 0) throw new UserNotFoundException(userId);
            return true; // User deleted
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to delete user", e);
        }
    }
}


