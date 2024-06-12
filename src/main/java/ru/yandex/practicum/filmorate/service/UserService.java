package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public void deleteUser(User user) {
        for (User usr : userStorage.findAll()) {
            usr.deleteFriend(user.getId());
        }
        userStorage.deleteUser(user);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public void addFriend(long id1, long id2) throws NotFoundException {
        User user1 = getUserByID(id1);
        User user2 = getUserByID(id2);
        user1.addFriend(id2);
        user2.addFriend(id1);
    }

    public void deleteFriend(long id1, long id2) throws NotFoundException {
        User user1 = getUserByID(id1);
        User user2 = getUserByID(id2);
        user1.deleteFriend(id2);
        user2.deleteFriend(id1);
    }

    public Set<User> getCommonFriends(long id1, long id2) throws NotFoundException {
        User user1 = getUserByID(id1);
        User user2 = getUserByID(id2);
        Set<Long> ids = user1.getFriends();
        ids.retainAll(user2.getFriends());
        return userStorage.findAll().stream().filter(user -> ids.contains(user.getId())).collect(Collectors.toSet());
    }

    public Set<User> getFriends(long id) throws NotFoundException {
        User user = getUserByID(id);
        Set<User> result = new HashSet<>();
        for (long friendId : user.getFriends()){
            User friend = getUserByID(friendId);
            result.add(friend);
        }
        return result;
    }

    private User getUserByID(long id) {
        return userStorage.findAll().stream()
                .filter(u -> u.getId() == id)
                .findAny()
                .orElseThrow(() -> new NotFoundException("User with id = " + id + " not found"));
    }
}
