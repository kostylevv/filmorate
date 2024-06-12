package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User updatedUser) {
        return userService.updateUser(updatedUser);
    }

    @DeleteMapping
    public void delete(@Valid @RequestBody User deletedUser) {
        userService.deleteUser(deletedUser);
    }

    @PutMapping("/users/{userId}/friends/{friendId}")
    public void addFriend(@PathVariable("userId") long userId, @PathVariable("friendId") long friendId) {
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/users/{userId}/friends/{friendId}")
    public void deleteFriend(@PathVariable("userId") long userId, @PathVariable("friendId") long friendId) {
        userService.deleteFriend(userId, friendId);
    }

    @GetMapping("/users/{userId}/friends")
    public Collection<User> getFriends(@PathVariable("userId") long userId) {
        return userService.getFriends(userId);
    }

    @GetMapping("/users/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable("id") long id, @PathVariable("otherId") long otherId) {
        return userService.getCommonFriends(id, otherId);
    }



}
