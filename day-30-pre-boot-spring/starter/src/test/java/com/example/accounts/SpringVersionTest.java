package com.example.accounts;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.core.SpringVersion;

/**
 * A guard, green from the start: moving onto Spring Boot must not take Spring Framework backwards.
 * The WAR runs on 5.3.39; whatever the build resolves after the move must be that or later.
 */
class SpringVersionTest {

    private static final String STARTER_VERSION = "5.3.39";

    @Test
    void springFrameworkIsNotDowngraded() {
        // The version spring-core's jar reports about itself (its manifest's Implementation-Version)
        String version = SpringVersion.getVersion();

        assertNotNull(version, "spring-core's jar has no Implementation-Version");
        assertTrue(atLeast(version, STARTER_VERSION),
                "Spring Framework on the classpath is " + version + ", older than the WAR's " + STARTER_VERSION
                        + ". Check what the build resolves: ./gradlew dependencyInsight --dependency spring-core");
    }

    // Compares numeric versions part by part, e.g. 5.3.31 < 5.3.39
    static boolean atLeast(String version, String minimum) {
        String[] have = version.split("[.-]");
        String[] need = minimum.split("[.-]");
        for (int i = 0; i < need.length; i++) {
            int a = i < have.length ? Integer.parseInt(have[i]) : 0;
            int b = Integer.parseInt(need[i]);
            if (a != b) {
                return a > b;
            }
        }
        return true;
    }
}
