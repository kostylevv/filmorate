package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.net.URI;
import java.time.LocalDate;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FilmControllerTest {
    String baseUrl;
    HttpHeaders headers;
    URI uri;

    @Autowired
    private FilmController controller;

    @Autowired
    private UserService userService;

    @Autowired
    private FilmService filmService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
    }

    @BeforeAll
    void setUp() throws Exception {
        baseUrl = "http://localhost:" + port + "/films";
        headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        uri = new URI(baseUrl);
    }

    @Test
    void filmIsAddedOk() {
        Film film = Film.builder().name("name").description("desc").releaseDate(LocalDate.now()).duration(1).build();
        HttpEntity<Film> request = new HttpEntity<>(film, headers);

        Assertions.assertTrue(this.restTemplate.postForEntity(uri, request, String.class)
                .getStatusCode().is2xxSuccessful());
    }

    @Test
    void emptyFilmIsNotAdded() {
        HttpEntity<String> request = new HttpEntity<>("", headers);
        Assertions.assertTrue(this.restTemplate.postForEntity(uri, request, String.class).getStatusCode().is5xxServerError());
    }

    @Test
    void filmWithWrongReleaseDateIsNotAdded() {
        Film film = Film.builder().name("name").description("desc")
                .releaseDate(LocalDate.of(1895,12,28)).duration(1).build();
        HttpEntity<Film> request = new HttpEntity<>(film, headers);
        Assertions.assertTrue(this.restTemplate.postForEntity(uri, request, String.class).getStatusCode().is4xxClientError());
    }

    @Test
    void filmWithNoTitleIsNotAdded() {
        Film film = Film.builder().name("").description("desc")
                .releaseDate(LocalDate.of(2703,12,12)).duration(1).build();
        HttpEntity<Film> request = new HttpEntity<>(film, headers);
        Assertions.assertTrue(this.restTemplate.postForEntity(uri, request, String.class).getStatusCode().is4xxClientError());
    }

    @Test
    void filmWithNoVeryLongDescriptionIsNotAdded() {
        StringBuilder sb = new StringBuilder();
        sb.repeat("D", 201);
        Film film = Film.builder().name("name").description(sb.toString())
                .releaseDate(LocalDate.of(2703,12,12)).duration(1).build();
        HttpEntity<Film> request = new HttpEntity<>(film, headers);
        Assertions.assertTrue(this.restTemplate.postForEntity(uri, request, String.class).getStatusCode().is4xxClientError());
    }

    @Test
    void filmWithLongDescriptionIsAdded() {
        StringBuilder sb = new StringBuilder();
        sb.repeat("D", 200);
        Film film = Film.builder().name("name").description(sb.toString())
                .releaseDate(LocalDate.of(2703,12,12)).duration(1).build();
        HttpEntity<Film> request = new HttpEntity<>(film, headers);
        Assertions.assertTrue(this.restTemplate.postForEntity(uri, request, String.class).getStatusCode().is2xxSuccessful());
    }

    @Test
    void filmWithNegativeDurationIsNotAdded() {
        Film film = Film.builder().name("name").description("d")
                .releaseDate(LocalDate.of(2703,12,12)).duration(-1).build();
        HttpEntity<Film> request = new HttpEntity<>(film, headers);
        Assertions.assertTrue(this.restTemplate.postForEntity(uri, request, String.class).getStatusCode().is4xxClientError());
    }

    @Test
    void filmWithZeroDurationIsNotAdded() {
        Film film = Film.builder().name("name").description("d")
                .releaseDate(LocalDate.of(2703,12,12)).duration(0).build();
        HttpEntity<Film> request = new HttpEntity<>(film, headers);
        Assertions.assertTrue(this.restTemplate.postForEntity(uri, request, String.class).getStatusCode().is4xxClientError());
    }

    @Test
    void correctFilmUpdateOk() {
        Film film = Film.builder().name("name").description("d")
                .releaseDate(LocalDate.of(2703,12,12)).duration(1).build();
        Film added = this.controller.create(film);
        long id = added.getId();
        added.setName("new name");
        HttpEntity<Film> request = new HttpEntity<>(added, headers);
        Assertions.assertTrue(this.restTemplate.exchange(uri, HttpMethod.PUT, request, String.class)
                .getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(added.getName(), this.controller.findAll()
                .stream()
                .filter(f -> f.getId() == id)
                .findFirst().orElseThrow(() -> {
                    throw new IllegalStateException("Film not found"); })
                .getName());
    }

    @Test
    void filmUpdateWithoutIdNotOk() {
        Film film = Film.builder().name("name").description("d")
                .releaseDate(LocalDate.of(2703,12,12)).duration(1).build();
        Film added = this.controller.create(film);
        added.setId(-1);
        HttpEntity<Film> request = new HttpEntity<>(added, headers);
        Assertions.assertTrue(this.restTemplate.exchange(uri, HttpMethod.PUT, request, String.class)
                .getStatusCode().is4xxClientError());
    }

    @Test
    void voidFilmUpdateNotOk() {
        HttpEntity<String> request = new HttpEntity<>("", headers);
        Assertions.assertTrue(this.restTemplate.exchange(uri, HttpMethod.PUT, request, String.class)
                .getStatusCode().is5xxServerError());
    }

    @Test
    void likeFilmIsOk() {
        User user1 = User.builder().name("User 1").birthday(LocalDate.now())
                .email("user1@gmail.com").login("user1").friends(new HashSet<>()).build();
        User user2 = User.builder().name("User 2").birthday(LocalDate.now())
                .email("user2@gmail.com").login("user2").friends(new HashSet<>()).build();
        User user3 = User.builder().name("Name").birthday(LocalDate.now())
                .email("user3@gmail.com").login("user3").friends(new HashSet<>()).build();

        userService.addUser(user1);
        userService.addUser(user2);
        userService.addUser(user3);

        Film film1 = Film.builder().name("name1").description("desc").releaseDate(LocalDate.now()).duration(1).build();
        Film film2 = Film.builder().name("name2").description("desc").releaseDate(LocalDate.now()).duration(2).build();
        Film film3 = Film.builder().name("name3").description("desc").releaseDate(LocalDate.now()).duration(3).build();

        filmService.addFilm(film1);
        filmService.addFilm(film2);
        filmService.addFilm(film3);

        Assertions.assertEquals(0, film1.getLikesCount());
        Assertions.assertEquals(0, film2.getLikesCount());
        Assertions.assertEquals(0, film3.getLikesCount());

        //filmService.like(film1.getId(), user1.getId());

        URI uri1 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/like/{userId}")
                .encode()
                .buildAndExpand(film1.getId(), user1.getId())
                .toUri();
        HttpEntity<String> request = new HttpEntity<>("", headers);
        ResponseEntity<String> response = this.restTemplate.exchange(uri1, HttpMethod.PUT, request, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        Assertions.assertEquals(1, film1.getLikesCount());
        Assertions.assertEquals(user1.getId(), film1.getLikes().stream().findFirst()
                .orElseThrow(() -> {
                    throw new IllegalStateException("Like failed"); }));
        Assertions.assertEquals(0, film2.getLikesCount());
        Assertions.assertEquals(0, film3.getLikesCount());
    }

    @Test
    void unlikeFilmIsOk() {
        User user1 = User.builder().name("User 1").birthday(LocalDate.now())
                .email("user1@gmail.com").login("user1").friends(new HashSet<>()).build();
        User user2 = User.builder().name("User 2").birthday(LocalDate.now())
                .email("user2@gmail.com").login("user2").friends(new HashSet<>()).build();

        userService.addUser(user1);
        userService.addUser(user2);

        Film film1 = Film.builder().name("name1").description("desc").releaseDate(LocalDate.now()).duration(1).build();
        Film film2 = Film.builder().name("name2").description("desc").releaseDate(LocalDate.now()).duration(2).build();
        Film film3 = Film.builder().name("name3").description("desc").releaseDate(LocalDate.now()).duration(3).build();

        filmService.addFilm(film1);
        filmService.addFilm(film2);
        filmService.addFilm(film3);

        filmService.like(film1.getId(), user1.getId());
        filmService.like(film1.getId(), user2.getId());
        filmService.like(film2.getId(), user1.getId());
        filmService.like(film3.getId(), user2.getId());


        URI uri1 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/like/{userId}")
                .encode()
                .buildAndExpand(film1.getId(), user1.getId())
                .toUri();
        HttpEntity<String> request = new HttpEntity<>("", headers);

        Assertions.assertEquals(2, film1.getLikesCount());

        ResponseEntity<String> response = this.restTemplate.exchange(uri1, HttpMethod.DELETE, request, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(1, film1.getLikesCount());
        Assertions.assertEquals(user2.getId(), film1.getLikes().stream().findFirst()
                .orElseThrow(() -> {
                    throw new IllegalStateException("Like failed"); }));
    }

    @Test
    void likeAndUnlikeUnexistingFilmIsNotOk() {
        User user1 = User.builder().name("User 1").birthday(LocalDate.now())
                .email("user1@gmail.com").login("user1").friends(new HashSet<>()).build();

        userService.addUser(user1);


        URI uri1 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/like/{userId}")
                .encode()
                .buildAndExpand(777, user1.getId())
                .toUri();
        HttpEntity<String> request = new HttpEntity<>("", headers);

        ResponseEntity<String> response = this.restTemplate.exchange(uri1, HttpMethod.PUT, request, String.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ResponseEntity<String> response1 = this.restTemplate.exchange(uri1, HttpMethod.DELETE, request, String.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response1.getStatusCode());
    }

    @Test
    void likeAndUnlikeFromUnexistingUserIsNotOk() {
        Film film1 = Film.builder().name("name1").description("desc").releaseDate(LocalDate.now()).duration(1).build();
        filmService.addFilm(film1);


        URI uri1 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/like/{userId}")
                .encode()
                .buildAndExpand(film1.getId(), 777)
                .toUri();
        HttpEntity<String> request = new HttpEntity<>("", headers);

        ResponseEntity<String> response = this.restTemplate.exchange(uri1, HttpMethod.PUT, request, String.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ResponseEntity<String> response1 = this.restTemplate.exchange(uri1, HttpMethod.DELETE, request, String.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response1.getStatusCode());
    }

    @Test
    void getPopularIsOk() throws Exception {
        User user1 = User.builder().name("User 1").birthday(LocalDate.now())
                .email("user1@gmail.com").login("user1").friends(new HashSet<>()).build();
        User user2 = User.builder().name("User 2").birthday(LocalDate.now())
                .email("user2@gmail.com").login("user2").friends(new HashSet<>()).build();
        User user3 = User.builder().name("Name").birthday(LocalDate.now())
                .email("user3@gmail.com").login("user3").friends(new HashSet<>()).build();
        User user4 = User.builder().name("User 1").birthday(LocalDate.now())
                .email("user1@gmail.com").login("user1").friends(new HashSet<>()).build();
        User user5 = User.builder().name("User 2").birthday(LocalDate.now())
                .email("user2@gmail.com").login("user2").friends(new HashSet<>()).build();
        User user6 = User.builder().name("Name").birthday(LocalDate.now())
                .email("user3@gmail.com").login("user3").friends(new HashSet<>()).build();
        User user7 = User.builder().name("User 1").birthday(LocalDate.now())
                .email("user1@gmail.com").login("user1").friends(new HashSet<>()).build();
        User user8 = User.builder().name("User 2").birthday(LocalDate.now())
                .email("user2@gmail.com").login("user2").friends(new HashSet<>()).build();
        User user9 = User.builder().name("Name").birthday(LocalDate.now())
                .email("user3@gmail.com").login("user3").friends(new HashSet<>()).build();
        User user10 = User.builder().name("User 1").birthday(LocalDate.now())
                .email("user1@gmail.com").login("user1").friends(new HashSet<>()).build();
        User user11 = User.builder().name("User 2").birthday(LocalDate.now())
                .email("user2@gmail.com").login("user2").friends(new HashSet<>()).build();
        User user12 = User.builder().name("Name").birthday(LocalDate.now())
                .email("user3@gmail.com").login("user3").friends(new HashSet<>()).build();

        userService.addUser(user1);
        userService.addUser(user2);
        userService.addUser(user3);
        userService.addUser(user4);
        userService.addUser(user5);
        userService.addUser(user6);
        userService.addUser(user7);
        userService.addUser(user8);
        userService.addUser(user9);
        userService.addUser(user10);
        userService.addUser(user11);
        userService.addUser(user12);

        Film film1 = Film.builder().name("name1").description("desc").releaseDate(LocalDate.now()).duration(1).build();
        Film film2 = Film.builder().name("name2").description("desc").releaseDate(LocalDate.now()).duration(2).build();
        Film film3 = Film.builder().name("name3").description("desc").releaseDate(LocalDate.now()).duration(3).build();
        Film film4 = Film.builder().name("name4").description("desc").releaseDate(LocalDate.now()).duration(1).build();
        Film film5 = Film.builder().name("name5").description("desc").releaseDate(LocalDate.now()).duration(2).build();
        Film film6 = Film.builder().name("name6").description("desc").releaseDate(LocalDate.now()).duration(3).build();
        Film film7 = Film.builder().name("name7").description("desc").releaseDate(LocalDate.now()).duration(1).build();
        Film film8 = Film.builder().name("name8").description("desc").releaseDate(LocalDate.now()).duration(2).build();
        Film film9 = Film.builder().name("name9").description("desc").releaseDate(LocalDate.now()).duration(3).build();
        Film film10 = Film.builder().name("name10").description("desc").releaseDate(LocalDate.now()).duration(1).build();
        Film film11 = Film.builder().name("name11").description("desc").releaseDate(LocalDate.now()).duration(2).build();
        Film film12 = Film.builder().name("name12").description("desc").releaseDate(LocalDate.now()).duration(3).build();

        filmService.addFilm(film1);
        filmService.addFilm(film2);
        filmService.addFilm(film3);
        filmService.addFilm(film4);
        filmService.addFilm(film5);
        filmService.addFilm(film6);
        filmService.addFilm(film7);
        filmService.addFilm(film8);
        filmService.addFilm(film9);
        filmService.addFilm(film10);
        filmService.addFilm(film11);
        filmService.addFilm(film12);

        for (int i = (int) film1.getId(); i <= (int)(film1.getId() + 12); i++) {
            for (int j = (int) user1.getId(); j <= (13 - i); j++) {
                filmService.like(i, j);
            }
        }

        URI uri1 = UriComponentsBuilder
                .fromUriString(baseUrl + "/popular?count={count}")
                .encode()
                .buildAndExpand(12)
                .toUri();
        HttpEntity<String> request = new HttpEntity<>("", headers);
        ResponseEntity<String> response = this.restTemplate.exchange(uri1, HttpMethod.GET, request, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        ObjectMapper mapper1 = new ObjectMapper();
        JsonNode root1 = mapper1.readTree(response.getBody());
        Assertions.assertEquals(12, root1.size());

        URI uri2 = UriComponentsBuilder
                .fromUriString(baseUrl + "/popular")
                .encode()
                .build()
                .toUri();

        ResponseEntity<String> response1 = this.restTemplate.exchange(uri2, HttpMethod.GET, request, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode root2 = mapper1.readTree(response1.getBody());
        Assertions.assertEquals(10, root2.size());
        System.out.println(response.getBody());
    }
}