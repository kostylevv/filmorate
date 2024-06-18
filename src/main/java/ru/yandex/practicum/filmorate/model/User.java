package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * User
 * @TODO add field confirmedFriend (bool)
 */
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

    @Builder.Default
    private Set<Long> friends = new HashSet<>();

    public Set<Long> getFriends() {
        return friends;
    }

    public boolean hasFriend(long friendId) {
        return friends.contains(friendId);
    }

    public void addFriend(long friendId) {
        if (hasFriend(friendId)) {
            log.warn("User with ID = {} already has friend with ID = {}", this.getId(), friendId);
        } else {
            friends.add(friendId);
            log.info("Added friend with ID = {} to user with ID = {}", friendId, this.getId());
        }
    }

    public void deleteFriend(long friendId) {
        if (!hasFriend(friendId)) {
            log.warn("User with ID = {} doesn't have friend with ID = {}", this.getId(), friendId);
        } else {
            friends.remove(friendId);
            log.info("Deleted friend with ID = {} from user with ID = {}", friendId, this.getId());
        }
    }
}
