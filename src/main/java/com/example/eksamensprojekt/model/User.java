package com.example.eksamensprojekt.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class User {

    private int userID;

    @Email(message = "Ugyldig e-mailadresse")
    @NotBlank(message = "E-mail må ikke være tom")
    private String email;

    private String passwordHash;
    private String name;
    private String title;
    private boolean external;


    public User(){

    }

    public User(int userID,
                String email,
                String passwordHash,
                String name,
                String title,
                boolean external){
        this.userID = userID;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.title = title;
        this.external = external;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
