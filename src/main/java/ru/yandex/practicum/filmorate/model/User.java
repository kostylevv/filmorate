package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class User {
    private long id;

    @Email
    private String email;

    @Pattern(regexp = "\\S+")
    private String login;

    private String password;

    private String name;

    @PastOrPresent
    private LocalDate birthday;
}
