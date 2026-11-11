package com.example.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** The whole app on an embedded Tomcat, on a random port, called over real HTTP. */
@SpringBootTest(webEnvironment = RANDOM_PORT)
class BootApplicationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void bootAppServesAccountsOnEmbeddedTomcat() {
        ResponseEntity<String> response = rest.getForEntity("/accounts", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"owner\":\"Ada\"", "\"currency\":\"GBP\"");
    }

    @Test
    void webXmlIsGone() {
        assertThat(Path.of("src/main/webapp/WEB-INF/web.xml")).doesNotExist();
    }
}
