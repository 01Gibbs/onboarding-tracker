package com.example.notifications;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

// Evidence for the incident ticket: this service uses the Log4j API, routed to SLF4J,
// but does not ship log4j-core, the only jar CVE-2021-44228 affects.
class Log4jExposureTest {

    @Test
    void log4jCoreIsNotOnTheClasspath() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.apache.logging.log4j.core.LifeCycle"));
    }
}
