package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film addFilm(Film film) {
        film.setId(getNextId());
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        films.put(film.getId(), film);
        log.info("Added film {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        try {
            checkFilm(film);
            if (film.getLikes() == null) {
                film.setLikes(new HashSet<>());
            }
            films.put(film.getId(), film);
            return film;
        } catch (ValidationException validationException) {
            log.error("Updated film id should be present");
            throw validationException;
        } catch (NotFoundException notFoundException) {
            log.error("Film with id = {} wasn't found and couldn't be updated", film.getId());
            throw notFoundException;
        }
    }

    @Override
    public void deleteFilm(Film film) {
        try {
            checkFilm(film);
            films.remove(film.getId());
        } catch (ValidationException validationException) {
            log.error("Film id to be deleted should be present");
            throw validationException;
        } catch (NotFoundException notFoundException) {
            log.error("Film with id = {} wasn't found and couldn't be deleted", film.getId());
            throw notFoundException;
        }
    }

    public Collection<Film> findAll() {
        return films.values();
    }

    private void checkFilm(Film film) {
        if (film.getId() == 0) {
            throw new ValidationException("ID не может быть пустым");
        }
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
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
