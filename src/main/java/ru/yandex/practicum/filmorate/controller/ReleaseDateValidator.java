package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ReleaseDateValidator implements
        ConstraintValidator<ReleaseDateConstraint, LocalDate> {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public void initialize(ReleaseDateConstraint releaseDate) {
    }

    @Override
    public boolean isValid(LocalDate releaseDate,
      ConstraintValidatorContext cxt) {
        return releaseDate.isAfter(MIN_RELEASE_DATE);
    }

}