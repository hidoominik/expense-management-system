package com.team.backend.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(unique = true, nullable = false, length = 45)
    @Size(min = 5, max = 45)
    @NotBlank(message = "Login is mandatory!")
    @Pattern(regexp = "^(?=.*[A-Za-z0-9]$)[A-Za-z][A-Za-z\\d.-]{4,45}$",
            message = "Incorrect format of a login (it could contain letters, numbers, -, " +
                    "it should start with letter, it should have 5-45 characters)!")
    private String login;

    @Column(unique = true, nullable = false, length = 100)
    @Size(min = 5, max = 100)
    @NotBlank(message = "Email is mandatory!")
    @Pattern(regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",
            message = "Incorrect format of an email (example@gmail.com)!")
    private String email;

    @Column
    @Size(max = 255)
    private String image;

    @Column(nullable = false)
    @Size(max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$",
            message = "Incorrect format of a password (min. 1 capital letter, min. 1 lowercase, min. 1 number, " +
                    "it should have min. 8 characters)!")
    private String password;

    @Column(length = 1)
    private String deleted;
}