package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;

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
        checkUsersExistence(id1, id2);
        User user1 = userStorage.findAll().stream().filter(u -> u.getId() == id1).findFirst().get();
        User user2 = userStorage.findAll().stream().filter(u -> u.getId() == id2).findFirst().get();
        user1.addFriend(id2);
        user2.addFriend(id1);
    }

    public void deleteFriend(long id1, long id2) throws NotFoundException {
        checkUsersExistence(id1, id2);
        User user1 = userStorage.findAll().stream().filter(u -> u.getId() == id1).findFirst().get();
        User user2 = userStorage.findAll().stream().filter(u -> u.getId() == id2).findFirst().get();
        user1.deleteFriend(id2);
        user2.deleteFriend(id1);
    }

    public Set<Long> commonFriends(long id1, long id2) throws NotFoundException {
        checkUsersExistence(id1, id2);
        User user1 = userStorage.findAll().stream().filter(u -> u.getId() == id1).findFirst().get();
        User user2 = userStorage.findAll().stream().filter(u -> u.getId() == id2).findFirst().get();
        Set<Long> result = user1.getFriends();
        result.retainAll(user2.getFriends());
        return result;
    }

    public Set<Long> getFriends(long id) {
        if (userStorage.findAll().stream().filter(u -> u.getId() == id).findFirst().isPresent()) {
            return userStorage.findAll().stream().filter(u -> u.getId() == id).findFirst().get().getFriends();
        } else {
            log.error("User with id = {} wasn't found", id);
            throw new NotFoundException("Пользователь с ID = " + id + " не найден");
        }

    }

    private void checkUsersExistence(long id1, long id2) {
        if (id1 == 0 || !findAll().contains(id1)) {
            log.error("Can't add friend to unexisting user with id = {}", id1);
            throw new NotFoundException("Пользователь c ID = " + id1 + " не найден");
        }

        if (id2 == 0 || !findAll().contains(id2)) {
            log.error("Unexisting user with id = {} can't be added as friend", id2);
            throw new NotFoundException("Пользователь c ID = " + id2 + " не найден");
        }
    }


}
