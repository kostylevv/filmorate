package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.controller.ReleaseDateConstraint;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {
    private long id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Size(max = 200, message
            = "Description must be between 0 and 200 characters")
    private String description;

    @ReleaseDateConstraint
    private LocalDate releaseDate;

    @Positive
    private int duration;
}
