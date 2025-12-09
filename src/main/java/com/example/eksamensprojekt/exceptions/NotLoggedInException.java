package com.example.eksamensprojekt.exceptions;

public class NotLoggedInException extends RuntimeException{
    public NotLoggedInException() {
        super("User is not logged in");
    }
}
