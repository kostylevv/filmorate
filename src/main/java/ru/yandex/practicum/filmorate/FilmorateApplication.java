package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.yandex.practicum.filmorate.model.DurationAdapter;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.LocalDateTypeAdapter;

import java.time.Duration;
import java.time.LocalDate;

@SpringBootApplication
public class FilmorateApplication {
	public static void main(String[] args) {
		SpringApplication.run(FilmorateApplication.class, args);

		Film film = new Film();
		film.setDescription("Desc");
		film.setName("Name");
		film.setReleaseDate(LocalDate.now());
		film.setDuration(Duration.ofMinutes(25));

		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter());
		builder.registerTypeAdapter(Duration.class, new DurationAdapter());
		Gson gson = builder.create();
		System.out.println(gson.toJson(film));

	}

}
