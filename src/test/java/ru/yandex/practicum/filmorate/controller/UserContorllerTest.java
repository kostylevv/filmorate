package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.net.URI;
import java.time.LocalDate;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserContorllerTest {
    String baseUrl;
    HttpHeaders headers;
    URI uri;

    @Autowired
    private UserController controller;

    @Autowired
    private UserService service;

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
        baseUrl = "http://localhost:" + port + "/users";
        headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        uri = new URI(baseUrl);
    }

    @Test
    void userIsAddedOk() {
        User user = User.builder().name("Name").birthday(LocalDate.now())
                .email("kostylev.v@gmail.com").login("kostylev.v").build();

        HttpEntity<User> request = new HttpEntity<>(user, headers);

        Assertions.assertTrue(this.restTemplate.postForEntity(uri, request, String.class)
                .getStatusCode().is2xxSuccessful());
    }

    @Test
    void userIsAddedWihoutNameAndLoginIsUsed() {
        User user = User.builder().birthday(LocalDate.now())
                .email("nonameuser@exampleml.com").login("expl.login").build();
        HttpEntity<User> request = new HttpEntity<>(user, headers);
        Assertions.assertTrue(this.restTemplate.postForEntity(uri, request, String.class)
                .getStatusCode().is2xxSuccessful());
        Assertions.assertTrue(this.controller.findAll().stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail())));
        Assertions.assertEquals(user.getLogin(), this.controller.findAll().stream()
                .filter(u -> u.getEmail().equals(user.getEmail()))
                .findFirst().get().getName());
    }

    @Test
    void userWithWrongEmailIsNotAdded() {
        User user = User.builder().birthday(LocalDate.now())
                .email("email").login("kostylev.v").build();

        HttpEntity<User> request = new HttpEntity<>(user, headers);

        Assertions.assertTrue(this.restTemplate.postForEntity(uri, request, String.class)
                .getStatusCode().is4xxClientError());
    }

    @Test
    void userWithWrongLoginIsNotAdded() {
        User user = User.builder().birthday(LocalDate.now())
                .email("email@gmail.cn").login("log in").build();

        HttpEntity<User> request = new HttpEntity<>(user, headers);

        Assertions.assertTrue(this.restTemplate.postForEntity(uri, request, String.class)
                .getStatusCode().is4xxClientError());
    }

    @Test
    void userBornInFutureIsNotAdded() {
        User user = User.builder().birthday(LocalDate.of(2100, 1, 1))
                .email("email@gmail.ru").login("mylogin").build();

        HttpEntity<User> request = new HttpEntity<>(user, headers);

        Assertions.assertTrue(this.restTemplate.postForEntity(uri, request, String.class)
                .getStatusCode().is4xxClientError());
    }

    @Test
    void userIsUpdatedOk() {
        User user = User.builder().name("name to be changed").birthday(LocalDate.now())
                .email("email@nottobechanged.com").login("login-not-to-be-changed").build();
        User added = this.controller.create(user);
        long id = added.getId();
        added.setName("new name");
        HttpEntity<User> request = new HttpEntity<>(added, headers);
        Assertions.assertTrue(this.restTemplate.exchange(uri, HttpMethod.PUT, request, String.class)
                .getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(added.getName(), this.controller.findAll()
                .stream()
                .filter(f -> f.getId() == id)
                .findFirst().get()
                .getName());

    }

    @Test
    void userUpdateWithoutIdNotOk() {
        User user = User.builder().name("Its my life").birthday(LocalDate.now())
                .email("kostylev.v@email.com").login("kostylev-v").build();
        User added = this.controller.create(user);
        added.setId(-1);
        HttpEntity<User> request = new HttpEntity<>(added, headers);
        Assertions.assertTrue(this.restTemplate.exchange(uri, HttpMethod.PUT, request, String.class)
                .getStatusCode().is5xxServerError());
    }

    @Test
    void voidUserUpdateNotOk() {
        HttpEntity<String> request = new HttpEntity<>("", headers);
        Assertions.assertTrue(this.restTemplate.exchange(uri, HttpMethod.PUT, request, String.class)
                .getStatusCode().is4xxClientError());
    }

    @Test
    void userAddFriendTest() {
        User user1 = User.builder().name("User 1").birthday(LocalDate.now())
                .email("user1@gmail.com").login("user1").friends(new HashSet<>()).build();
        User user2 = User.builder().name("User 2").birthday(LocalDate.now())
                .email("user2@gmail.com").login("user2").friends(new HashSet<>()).build();
        User user3 = User.builder().name("Name").birthday(LocalDate.now())
                .email("user3@gmail.com").login("user3").friends(new HashSet<>()).build();

        HttpEntity<User> request1 = new HttpEntity<>(user1, headers);
        HttpEntity<User> request2 = new HttpEntity<>(user2, headers);
        HttpEntity<User> request3 = new HttpEntity<>(user3, headers);

        this.restTemplate.postForEntity(uri, request1, String.class);
        this.restTemplate.postForEntity(uri, request2, String.class);
        this.restTemplate.postForEntity(uri, request3, String.class);

        Assertions.assertTrue(service.getFriends(1).size() == 0);
        Assertions.assertTrue(service.getFriends(2).size() == 0);
        Assertions.assertTrue(service.getFriends(3).size() == 0);

        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/users/{id}/friends/{friendId}")
                .encode()
                .buildAndExpand("1", "2")
                .toUri();

        this.restTemplate.put(uri, new HttpEntity<>(headers));
        Assertions.assertTrue(service.getFriends(1).size() == 1);


    }

}