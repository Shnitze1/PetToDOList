package com.example.pet_todolist.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Login cannot be empty.")
    @Size(min = 3, max = 30, message = "The login must be between 3 and 30 characters.")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Login can only contain letters, numbers, underscores, and hyphens.")
    private String login;

    @Pattern(regexp = "^(?=.*[A-Z]).{7,64}$", message = "Password must be between 7 and 64 characters long and contain at least one uppercase letter.")
    @ToString.Exclude
    private String password;

    @Column(name = "refreshtoken")
    @JsonIgnore
    @ToString.Exclude
    private String refreshToken;
}
