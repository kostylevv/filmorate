package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

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

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Added film {}", film);
        return film;
    }


    @PutMapping
    public Film update(@Valid @RequestBody Film updatedFilm) {
        if (updatedFilm.getId() == 0) {
            log.error("Updated film id should be present");
            throw new ValidationException("ID не может быть пустым");
        }

        if (films.containsKey(updatedFilm.getId())) {
            films.remove(updatedFilm.getId());
            films.put(updatedFilm.getId(), updatedFilm);
            log.info("Updated film {}", updatedFilm);
            return updatedFilm;
        } else {
            log.error("Film with id = {} wasn't found", updatedFilm.getId());
            throw new NotFoundException("Фильм с id = " + updatedFilm.getId() + " не найден");
        }
    }



    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}