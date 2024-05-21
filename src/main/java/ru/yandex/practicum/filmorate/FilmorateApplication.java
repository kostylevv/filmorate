package ru.yandex.practicum.filmorate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@SpringBootApplication
public class FilmorateApplication {
	public static void main(String[] args) {
		SpringApplication.run(FilmorateApplication.class, args);
		Film film = new Film();
		film.setDescription("Desc");
		film.setName("Name");
		film.setReleaseDate(LocalDate.now());
		film.setDuration(60);
	}

}
