package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.net.URI;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

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
                .findFirst().orElseThrow(() -> new IllegalArgumentException("User not found")).getName());
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
                .findFirst().orElseThrow(() -> {throw new IllegalStateException("User not found");})
                .getName());

    }

    @Test
    void userUpdateWithoutIdNotOk() {
        User user = User.builder().name("Its my life").birthday(LocalDate.now())
                .email("kostylev.v@email.com").login("kostylev-v").build();
        User added = this.controller.create(user);
        added.setId(-1);
        HttpEntity<User> request = new HttpEntity<>(added, headers);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, this.restTemplate.exchange(uri, HttpMethod.PUT, request, String.class)
                .getStatusCode());
    }

    @Test
    void voidUserUpdateNotOk() {
        HttpEntity<String> request = new HttpEntity<>("", headers);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, this.restTemplate.exchange(uri, HttpMethod.PUT, request, String.class)
                .getStatusCode());
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

        Assertions.assertEquals(0, service.getFriends(1).size());
        Assertions.assertEquals(0, service.getFriends(2).size());
        Assertions.assertEquals(0, service.getFriends(3).size());

        URI uri1 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends/{friendId}")
                .encode()
                .buildAndExpand("1", "2")
                .toUri();

        this.restTemplate.put(uri1, new HttpEntity<>(headers));
        Assertions.assertEquals(1, service.getFriends(1).size());
        Assertions.assertEquals(1, service.getFriends(2).size());
        Assertions.assertEquals(0, service.getFriends(3).size());

        URI uri2 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends/{friendId}")
                .encode()
                .buildAndExpand("3", "1")
                .toUri();

        this.restTemplate.put(uri2, new HttpEntity<>(headers));
        Assertions.assertEquals(2, service.getFriends(1).size());
        Assertions.assertEquals(1, service.getFriends(2).size());
        Assertions.assertEquals(1, service.getFriends(3).size());

        URI uri4 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends/{friendId}")
                .encode()
                .buildAndExpand("454", "1")
                .toUri();
        HttpEntity<String> request = new HttpEntity<>("", headers);
        ResponseEntity<String> response = this.restTemplate.exchange(uri4, HttpMethod.PUT, request, String.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteFriendTest() {
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

        URI uri1 = UriComponentsBuilder
                .fromUriString(baseUrl + "/users/{id}/friends/{friendId}")
                .encode()
                .buildAndExpand("1", "2")
                .toUri();

        this.restTemplate.put(uri1, new HttpEntity<>(headers));


        URI uri2 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends/{friendId}")
                .encode()
                .buildAndExpand("3", "1")
                .toUri();

        this.restTemplate.put(uri2, new HttpEntity<>(headers));

        Assertions.assertEquals(2, service.getFriends(1).size());

        URI uri3 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends/{friendId}")
                .encode()
                .buildAndExpand("1", "2")
                .toUri();
       this.restTemplate.delete(uri3);

        Assertions.assertEquals(1, service.getFriends(1).size());
        Assertions.assertEquals(0, service.getFriends(2).size());
        Assertions.assertEquals(1, service.getFriends(3).size());

        URI uri4 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends/{friendId}")
                .encode()
                .buildAndExpand("1", "3")
                .toUri();
        this.restTemplate.delete(uri4);

        Assertions.assertEquals(0, service.getFriends(1).size());
        Assertions.assertEquals(0, service.getFriends(2).size());
        Assertions.assertEquals(0, service.getFriends(3).size());
    }

    @Test
    void getFriendsTest() throws NullPointerException, JsonProcessingException {

        User user1 = User.builder().name("User 124").birthday(LocalDate.now())
                .email("user124@gmail.com").login("user124").friends(new HashSet<>()).build();
        User user2 = User.builder().name("User 2").birthday(LocalDate.now())
                .email("user245@gmail.com").login("user246").friends(new HashSet<>()).build();
        User user3 = User.builder().name("Name").birthday(LocalDate.now())
                .email("user344@gmail.com").login("user344").friends(new HashSet<>()).build();

        HttpEntity<User> request1 = new HttpEntity<>(user1, headers);
        HttpEntity<User> request2 = new HttpEntity<>(user2, headers);
        HttpEntity<User> request3 = new HttpEntity<>(user3, headers);

        User u1 = this.restTemplate.postForEntity(uri, request1, User.class).getBody();
        User u2 = this.restTemplate.postForEntity(uri, request2, User.class).getBody();
        User u3 = this.restTemplate.postForEntity(uri, request3, User.class).getBody();


        URI uri1 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends")
                .encode()
                .buildAndExpand(Objects.requireNonNull(u1).getId())
                .toUri();

        HttpEntity<String> request = new HttpEntity<>("", headers);
        ResponseEntity<String> response = this.restTemplate.exchange(uri1, HttpMethod.GET, request, String.class);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        Assertions.assertEquals(0, root.size());

        URI uri2 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends/{friendId}")
                .encode()
                .buildAndExpand(u1.getId(), Objects.requireNonNull(u2).getId())
                .toUri();

        this.restTemplate.put(uri2, new HttpEntity<>(headers));

        URI uri3 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends/{friendId}")
                .encode()
                .buildAndExpand(Objects.requireNonNull(u3).getId(), u1.getId())
                .toUri();

        this.restTemplate.put(uri3, new HttpEntity<>(headers));

        URI uri4 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends")
                .encode()
                .buildAndExpand(u1.getId())
                .toUri();
        ResponseEntity<String> response1 = this.restTemplate.exchange(uri4, HttpMethod.GET, request, String.class);
        Assertions.assertTrue(response1.getStatusCode().is2xxSuccessful());
        ObjectMapper mapper1 = new ObjectMapper();
        JsonNode root1 = mapper1.readTree(response1.getBody());
        Assertions.assertEquals(2, root1.size());
    }

    @Test
    void getCommonFriendsTest() throws Exception {

        User user1 = User.builder().name("User 124").birthday(LocalDate.now())
                .email("user124@gmail.com").login("user124").friends(new HashSet<>()).build();
        User user2 = User.builder().name("User 246").birthday(LocalDate.now())
                .email("user245@gmail.com").login("user246").friends(new HashSet<>()).build();
        User user3 = User.builder().name("Name 344").birthday(LocalDate.now())
                .email("user344@gmail.com").login("user344").friends(new HashSet<>()).build();
        User user4 = User.builder().name("Name 56").birthday(LocalDate.now())
                .email("user56@gmail.com").login("user56").friends(new HashSet<>()).build();

        HttpEntity<User> request1 = new HttpEntity<>(user1, headers);
        HttpEntity<User> request2 = new HttpEntity<>(user2, headers);
        HttpEntity<User> request3 = new HttpEntity<>(user3, headers);
        HttpEntity<User> request4 = new HttpEntity<>(user4, headers);

        User u1 = this.restTemplate.postForEntity(uri, request1, User.class).getBody();
        User u2 = this.restTemplate.postForEntity(uri, request2, User.class).getBody();
        User u3 = this.restTemplate.postForEntity(uri, request3, User.class).getBody();
        User u4 = this.restTemplate.postForEntity(uri, request4, User.class).getBody();


        URI uri1 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends")
                .encode()
                .buildAndExpand(Objects.requireNonNull(u1).getId())
                .toUri();

        HttpEntity<String> request = new HttpEntity<>("", headers);
        ResponseEntity<String> response = this.restTemplate.exchange(uri1, HttpMethod.GET, request, String.class);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        Assertions.assertEquals(0, root.size());

        URI uri2 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends/{friendId}")
                .encode()
                .buildAndExpand(u1.getId(), Objects.requireNonNull(u2).getId())
                .toUri();
        URI uri3 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends/{friendId}")
                .encode()
                .buildAndExpand(u1.getId(), Objects.requireNonNull(u3).getId())
                .toUri();
        URI uri4 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends/{friendId}")
                .encode()
                .buildAndExpand(u1.getId(), Objects.requireNonNull(u4).getId())
                .toUri();

        this.restTemplate.put(uri2, new HttpEntity<>(headers));
        this.restTemplate.put(uri3, new HttpEntity<>(headers));
        this.restTemplate.put(uri4, new HttpEntity<>(headers));

        URI uri5 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends/{friendId}")
                .encode()
                .buildAndExpand(u2.getId(), u4.getId())
                .toUri();

        this.restTemplate.put(uri5, new HttpEntity<>(headers));

        URI uri6 = UriComponentsBuilder
                .fromUriString(baseUrl + "/{id}/friends/common/{otherId}")
                .encode()
                .buildAndExpand(u1.getId(), u2.getId())
                .toUri();
        ResponseEntity<String> response1 = this.restTemplate.exchange(uri6, HttpMethod.GET, request, String.class);
        Assertions.assertTrue(response1.getStatusCode().is2xxSuccessful());
        System.out.println(response1.getBody());
        Assertions.assertTrue(Objects.requireNonNull(response1.getBody()).contains("{\"id\":" + u4.getId() + ","));
    }

}