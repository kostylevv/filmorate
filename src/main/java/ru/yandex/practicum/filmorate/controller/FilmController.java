package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for films
 */

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESC_LENGTH = 200;

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Title shouldn't be empty");
            throw new ValidationException("название не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > MAX_DESC_LENGTH) {
            log.error("Max description length is {} , got {} ", MAX_DESC_LENGTH, film.getDescription().length());
            throw new ValidationException("максимальная длина описания - " + MAX_DESC_LENGTH + " символов");
        }

        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Release date couldn't be before {}, got {}", MIN_RELEASE_DATE, film.getReleaseDate());
            throw new ValidationException("дата релиза — не раньше " + MIN_RELEASE_DATE);
        }

        if (film.getDuration() == null || film.getDuration().isZero() || film.getDuration().isNegative()) {
            log.error("Film duration should be positive, got {}", film.getDuration());
            throw new ValidationException("продолжительность фильма должна быть положительным числом");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.trace("Added film {}", film);
        return film;
    }

    /*
    @PutMapping
    public Post update(@RequestBody Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с id = " + newPost.getId() + " не найден");
    }
*/
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}