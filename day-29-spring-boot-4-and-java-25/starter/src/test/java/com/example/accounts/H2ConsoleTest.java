package com.example.accounts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

// The H2 console is its own servlet, outside Spring MVC, so MockMvc can't see it.
// This test starts a real server on a random port instead.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class H2ConsoleTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void h2ConsoleIsOffOutsideTheDevProfile() {
        assertThat(restTemplate.getForEntity("/h2-console/", String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
