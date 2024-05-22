package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Added user {}", user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User updatedUser) {
        if (updatedUser.getId() == 0) {
            log.error("Updated user id should be present");
            throw new ValidationException("ID не может быть пустым");
        }

        if (users.containsKey(updatedUser.getId())) {
            users.remove(updatedUser.getId());
            if (updatedUser.getName() == null || updatedUser.getName().isBlank()) {
                updatedUser.setName(updatedUser.getLogin());
            }
            users.put(updatedUser.getId(), updatedUser);
            log.info("Updated user {}", updatedUser);
            return updatedUser;
        } else {
            log.error("User with id = {} wasn't found", updatedUser.getId());
            throw new NotFoundException("Пользователь с id = " + updatedUser.getId() + " не найден");
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public String handleMyValidationExceptions(
            ValidationException ex) {
        return ex.getMessage();
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
