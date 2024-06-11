package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@Slf4j
public class User {
    private long id;

    @Email
    private String email;

    @Pattern(regexp = "\\S+")
    private String login;

    private String password;

    private String name;

    @PastOrPresent
    private LocalDate birthday;

    private Set<Long> friends;

    public Set<Long> getFriends() {
        return friends;
    }

    public boolean hasFriend(long friendId) {
        return friends.contains(friendId);
    }

    public void addFriend(long friendId) {
        if (hasFriend(friendId)) {
            log.warn("User with ID = {} already has friend with ID = {}", this.getId(), friendId);
        }
        friends.add(friendId);
        log.trace("Added friend with ID = {} to user with ID = {}", friendId, this.getId());
    }

    public void deleteFriend(long friendId) {
        if (hasFriend(friendId)) {
            log.warn("User with ID = {} doesn't have friend with ID = {}", this.getId(), friendId);
        }
        friends.remove(friendId);
        log.trace("Deleted friend with ID = {} from user with ID = {}", friendId, this.getId());
    }
}
