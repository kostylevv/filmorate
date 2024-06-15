package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

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
        if (!films.containsKey(film.getId())) {
            log.error("Film with id = {} not found", film.getId());
            throw new NotFoundException("Фильм с ID = " + film.getId() + " не найден");
        }
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        log.info("Updated film {}", film);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void deleteFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            log.error("Film with id = {} not found", film.getId());
            throw new NotFoundException("Фильм с ID = " + film.getId() + " не найден");
        }
        log.info("Deleted film {}", film);
        films.remove(film.getId());
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    public Film getFilmById(long id) {
        if (!films.containsKey(id)) {
            log.error("Film with id = {} not found", id);
            throw new NotFoundException("Фильм с ID = " + id + " не найден");
        }
        return films.get(id);
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
