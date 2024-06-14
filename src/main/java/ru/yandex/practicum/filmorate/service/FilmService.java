package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private FilmStorage filmStorage;
    private UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public void deleteFilm(Film film) {
        filmStorage.deleteFilm(film);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public void like(long filmId, long userId) {
        User liker = userStorage.getUserById(userId);
        Film liked = getFilmById(filmId);
        liked.like(liker.getId());
    }

    public void unLike(long filmId, long userId) {
        User unliker = userStorage.getUserById(userId);
        Film unliked = getFilmById(filmId);
            unliked.unLike(unliker.getId());
    }

    public List<Film> getMostLiked(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void removeAllUserLikes(long userId) {
        for (Film film : findAll()) {
            film.unLike(userId);
        }
    }

    public Film getFilmById(long id) {
        return filmStorage.getFilmById(id);
    }
}
