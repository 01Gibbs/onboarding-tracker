package com.example.accounts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Red on purpose until the toolchain is Java 11. This test can't use Runtime.version():
 * that method arrived in Java 9, so it wouldn't compile on Java 8.
 */
class JavaVersionTest {

    @Test
    void runsOnJava11OrLater() {
        // "1.8" on Java 8, then "9", "10", "11", ... from Java 9 onwards (JEP 223)
        String spec = System.getProperty("java.specification.version");
        int feature = spec.startsWith("1.") ? Integer.parseInt(spec.substring(2)) : Integer.parseInt(spec);

        assertThat(feature)
                .as("the tests run on Java %s; this service must move to Java 11", spec)
                .isGreaterThanOrEqualTo(11);
    }
}
