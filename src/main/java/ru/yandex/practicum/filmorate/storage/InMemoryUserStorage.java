package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        users.put(user.getId(), user);
        log.info("Added user {}", user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        try {
            checkUser(user);
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            if (user.getFriends() == null) {
                user.setFriends(new HashSet<>());
            }
            log.info("Updated user {}", user);
            users.put(user.getId(), user);
            return user;
        } catch (ValidationException validationException) {
            log.error("Updated user ID should be provided");
            throw validationException;
        } catch (NotFoundException notFoundException) {
            log.error("User not found");
            throw notFoundException;
        }
    }

    @Override
    public void deleteUser(User user) {
        try {
            checkUser(user);
            users.remove(user.getId());
        } catch (ValidationException validationException) {
            log.error("Deleted user ID should be provided");
            throw validationException;
        } catch (NotFoundException notFoundException) {
            log.error("User not found");
            throw notFoundException;
        }
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    private void checkUser(User user) {
        if (user.getId() == 0) {
            throw new ValidationException("ID не может быть пустым");
        }
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
