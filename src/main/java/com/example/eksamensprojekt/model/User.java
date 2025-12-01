package com.example.eksamensprojekt.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

public class User {

    private int userId;

    @Email(message = "Ugyldig e-mailadresse")
    @NotBlank(message = "E-mail må ikke være tom")
    @Size(max = 254, message = "E-mail kan ikke være mere end 254 tegn")
    private String email;

    @NotBlank(message = "Password må ikke være tom")
    @Size(max = 255)
    private String passwordHash;


    @NotBlank(message = "Navn må ikke være tom")
    @Size(max = 100, message = "Navn kan ikke være mere end 100 tegn")
    private String name;

    @Size(max = 100, message = "Navn kan ikke være mere end 100 tegn")
    private String title;

    private boolean external;


    public User() {

    }

    public User(int userId,
                String email,
                String passwordHash,
                String name,
                String title,
                boolean external) {
        this.userId = userId;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return userId == user.userId && external == user.external && Objects.equals(email, user.email) && Objects.equals(passwordHash, user.passwordHash) && Objects.equals(name, user.name) && Objects.equals(title, user.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email, passwordHash, name, title, external);
    }
}
