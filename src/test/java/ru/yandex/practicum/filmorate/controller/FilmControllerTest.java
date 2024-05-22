package ru.yandex.practicum.filmorate.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.net.URI;
import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilmControllerTest {

    @Autowired
    private FilmController controller;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    void contextLoads() throws Exception {
        assertThat(controller).isNotNull();
    }

    @Test
    void filmIsAddedOk() throws Exception {
        final String baseUrl = "http://localhost:"+port+"/films";
        Film film = Film.builder().name("name").description("desc").releaseDate(LocalDate.now()).duration(1).build();
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-COM-PERSIST", "true");

        HttpEntity<Film> request = new HttpEntity<>(film, headers);

        assertThat(this.restTemplate.postForEntity(uri, request, String.class).getStatusCode().is2xxSuccessful());
    }

    @Test
    void emptyFilmIsNotAdded() throws Exception {
        final String baseUrl = "http://localhost:"+port+"/films";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-COM-PERSIST", "true");

        HttpEntity<Film> request = new HttpEntity<>(null, headers);

        assertThat(this.restTemplate.postForEntity(uri, request, String.class).getStatusCode().is5xxServerError());
    }

    @Test
    void filmWithWrongReleaseDateIsNotAdded() throws Exception {
        final String baseUrl = "http://localhost:"+port+"/films";
        Film film = Film.builder().name("name").description("desc")
                .releaseDate(LocalDate.of(1703,12,12)).duration(1).build();
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-COM-PERSIST", "true");

        HttpEntity<Film> request = new HttpEntity<>(film, headers);

        assertThat(this.restTemplate.postForEntity(uri, request, String.class).getStatusCode().is4xxClientError());
    }




}