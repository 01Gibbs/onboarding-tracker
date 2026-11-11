package com.example.accounts.versions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.core.Version;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.json.JsonMapper;

/**
 * Red on purpose: the ticket says jackson-databind must be at least 3.1.8, and Spring Boot 4.1.2
 * manages 3.1.7. Turn it green by overriding the version the Maven way, not by editing this test.
 * The Jackson modules are released together, so jackson-core must move with jackson-databind.
 */
class JacksonVersionTest {

    private static final Version REQUIRED = new Version(3, 1, 8, null, "tools.jackson.core", "jackson-databind");

    @Test
    void jacksonDatabindIsAtLeast3_1_8() {
        // The version the jar on the test classpath reports about itself
        Version resolved = JsonMapper.builder().build().version();

        assertThat(resolved)
                .as("jackson-databind on the classpath is %s; the ticket needs %s or later", resolved, REQUIRED)
                .isGreaterThanOrEqualTo(REQUIRED);

        // Compare the numbers only: Version.equals also compares the artifact names
        Version core = new JsonFactory().version();
        assertThat(core.toString())
                .as("jackson-core on the classpath is %s but jackson-databind is %s; the Jackson modules move together",
                        core, resolved)
                .isEqualTo(resolved.toString());
    }
}
