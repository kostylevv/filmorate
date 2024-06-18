package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.controller.ReleaseDateConstraint;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 * @TODO add fields: genre, MPA-rating
 *
 */
@Data
@Builder
@Slf4j
public class Film {
    private long id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Size(max = 200, message
            = "Description must be between 0 and 200 characters")
    private String description;

    @ReleaseDateConstraint
    private LocalDate releaseDate;

    @Positive
    private int duration;

    @Builder.Default
    private Set<Long> likes = new HashSet<>();

    public int getLikesCount() {
        return likes.size();
    }

    public Set<Long> getLikes() {
        return likes;
    }

    public void like(long userId) {
        if (isLiked(userId)) {
            log.warn("User with id {} already liked film with id {}", userId, id);
        } else {
            likes.add(userId);
            log.info("Added like to film with id {} from user with id {}", id, userId);
        }
    }

    public void unLike(long userId) {
        if (!isLiked(userId)) {
            log.warn("User with id {} aren't liked film with id {}, can't unlike", userId, id);
        } else {
            likes.remove(userId);
            log.info("User with id {} unliked film with id {}", userId, id);
        }
    }

    private boolean isLiked(long userId) {
        return likes.contains(userId);
    }
}
