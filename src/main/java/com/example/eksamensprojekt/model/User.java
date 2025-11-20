package com.example.eksamensprojekt.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class User {

    @Email(message = "Ugyldig e-mailadresse")
    @NotBlank(message = "E-mail må ikke være tom")
    private String email;

    @Size(min = 6, message = "Password skal være mindst 6 tegn")
    private String password;

    public User(){

    }

    public User(String email, String password){
        this.email = email;
        this.password = password;
    }
}
