package com.example.accounts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootVersion;

// Red on purpose until the upgrade is done: the target platform is Java 25 and Spring Boot 4.
// Plain JUnit, no Spring context, so it compiles and runs the same before and after the upgrade.
class PlatformVersionTest {

    @Test
    void runsOnJava25OrLater() {
        assertThat(Runtime.version().feature())
                .as("Java feature version the tests run on")
                .isGreaterThanOrEqualTo(25);
    }

    @Test
    void runsOnSpringBoot4() {
        assertThat(SpringBootVersion.getVersion())
                .as("Spring Boot version on the classpath")
                .startsWith("4.");
    }
}
