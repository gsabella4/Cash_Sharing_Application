package com.techelevator.tenmo.model;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;

public class RegisterUserDTO {


    @NotEmpty
    @NotBlank
    @Pattern(regexp = "[a-zA-Z0-9]+", message = "Username must not contain spaces or special characters")
    @Size(min = 4, max = 28, message = "Username must be at least 4 characters")
    private String username;

    @NotEmpty
    @Size(min = 8, max = 128, message = "Password must be at least 8 characters")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
