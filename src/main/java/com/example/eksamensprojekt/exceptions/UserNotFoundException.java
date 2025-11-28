package com.example.eksamensprojekt.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(int userId) {
        super("User not found: id=" + userId);
    }

    public UserNotFoundException(String email) {
        super("User not found: email=" + email);
    }
}
