package ru.yandex.practicum.filmorate.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import ru.yandex.practicum.filmorate.model.Film;

import java.net.URI;
import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FilmControllerTest {
    String baseUrl;
    HttpHeaders headers;
    URI uri;

    @Autowired
    private FilmController controller;

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
        baseUrl = "http://localhost:"+port+"/films";
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
        Assertions.assertTrue(this.restTemplate.postForEntity(uri, request, String.class).getStatusCode().is4xxClientError());
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
                .findFirst().get()
                .getName());
    }

    @Test
    void filmUpdateWithoutIdNotOk() {
        Film film = Film.builder().name("name").description("d")
                .releaseDate(LocalDate.of(2703,12,12)).duration(1).build();
        Film added = this.controller.create(film);
        added.setId(this.controller.findAll().size()+1);
        HttpEntity<Film> request = new HttpEntity<>(added, headers);
        Assertions.assertTrue(this.restTemplate.exchange(uri, HttpMethod.PUT, request, String.class)
                .getStatusCode().is5xxServerError());
    }

    @Test
    void voidFilmUpdateWithoutIdNotOk() {
        HttpEntity<String> request = new HttpEntity<>("", headers);
        Assertions.assertTrue(this.restTemplate.exchange(uri, HttpMethod.PUT, request, String.class)
                .getStatusCode().is4xxClientError());
    }

}