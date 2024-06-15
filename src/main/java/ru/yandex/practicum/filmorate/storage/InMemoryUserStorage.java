package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
        if (!users.containsKey(user.getId())) {
            log.error("User wih id = {} not found", user.getId());
            throw new NotFoundException("Пользователь с ID = " + user.getId() + " не найден");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        log.info("Updated user {}", user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUser(User user) {
        if (!users.containsKey(user.getId())) {
            log.error("User wih id = {} not found", user.getId());
            throw new NotFoundException("Пользователь с ID = " + user.getId() + " не найден");
        }
        log.info("Deleted user {}", user);
        users.remove(user.getId());
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User getUserById(long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            log.error("User wih id = {} not found", id);
            throw new NotFoundException("Пользователь с ID = " + id + " не найден");
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
