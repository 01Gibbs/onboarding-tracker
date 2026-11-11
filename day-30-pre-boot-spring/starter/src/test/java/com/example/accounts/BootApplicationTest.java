package com.example.accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Red until the app is a Spring Boot app.
 *
 * <p>It can't use {@code @SpringBootTest} yet: Spring Boot isn't on the classpath, so that wouldn't compile and
 * the whole build would fail. Instead it looks Spring Boot and your application class up by name, starts the app
 * on a random port and calls {@code GET /accounts} over HTTP.
 */
class BootApplicationTest {

    private static final String APPLICATION_CLASS = "com.example.accounts.AccountsApplication";

    // TODO: bootAppServesAccountsOnEmbeddedTomcat — once Spring Boot is in, you may rewrite this test as
    //       @SpringBootTest(webEnvironment = RANDOM_PORT) with TestRestTemplate. Keep the name and what it checks.
    @Test
    void bootAppServesAccountsOnEmbeddedTomcat() throws Exception {
        Class<?> application = load(APPLICATION_CLASS,
                "No " + APPLICATION_CLASS + " yet: add a @SpringBootApplication class with a main method");
        Class<?> springApplication = load("org.springframework.boot.SpringApplication",
                "Spring Boot isn't on the classpath yet: add the Spring Boot plugin and spring-boot-starter-web");

        Object started = springApplication.getMethod("run", Class.class, String[].class)
                .invoke(null, application, new String[] {"--server.port=0"});
        try (ConfigurableApplicationContext context = (ConfigurableApplicationContext) started) {
            String port = context.getEnvironment().getProperty("local.server.port");
            assertTrue(port != null, "No embedded web server started (local.server.port is not set)");

            HttpResponse<String> response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/accounts")).build(),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("\"owner\":\"Ada\""), response.body());
            assertTrue(response.body().contains("\"currency\":\"GBP\""), response.body());
        }
    }

    @Test
    void webXmlIsGone() {
        assertFalse(Files.exists(Path.of("src/main/webapp/WEB-INF/web.xml")),
                "web.xml is still there: Spring Boot's embedded Tomcat ignores it, so delete it");
    }

    private static Class<?> load(String className, String messageIfMissing) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return fail(messageIfMissing);
        }
    }
}
