package com.example.statements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

// Evidence for the incident ticket: the log4j-core this service ships is a fixed release,
// and it's the one version the whole estate declares in the version catalog.
class StatementsLog4jVersionTest {

    static final String MINIMUM = "2.17.1";

    // Tests run in the service's own folder; the catalog is at the top of the build
    static final Path CATALOG = Path.of("../../gradle/libs.versions.toml");

    @Test
    void log4jCoreIsPatched() throws ClassNotFoundException {
        String version = loadedLog4jCoreVersion();

        assertTrue(atLeast(version, MINIMUM),
                "This service ships log4j-core " + version + ", which is vulnerable. It needs " + MINIMUM + " or later.");
    }

    @Test
    void log4jCoreIsTheVersionInTheCatalog() throws ClassNotFoundException, IOException {
        String declared = catalogLog4jVersion();
        assertTrue(atLeast(declared, MINIMUM),
                "gradle/libs.versions.toml declares log4j " + declared + ", which is vulnerable.");

        String version = loadedLog4jCoreVersion();
        assertEquals(declared, version,
                "This service ships log4j-core " + version + ", but gradle/libs.versions.toml declares " + declared
                        + ". The estate has one Log4j version, set in the catalog.");
    }

    static String loadedLog4jCoreVersion() throws ClassNotFoundException {
        // Load a class from log4j-core at run time: this service doesn't use Log4j directly,
        // it arrives through a shared library, so it isn't on our compile classpath.
        Class<?> core = Class.forName("org.apache.logging.log4j.core.LifeCycle");
        // The version comes from the jar's manifest (Implementation-Version)
        String version = core.getPackage().getImplementationVersion();
        assertNotNull(version, "log4j-core's jar has no Implementation-Version");
        return version;
    }

    // The [versions] entry: log4j = "x.y.z"
    static String catalogLog4jVersion() throws IOException {
        Matcher entry = Pattern.compile("(?m)^log4j\\s*=\\s*\"([^\"]+)\"").matcher(Files.readString(CATALOG));
        assertTrue(entry.find(), "gradle/libs.versions.toml has no log4j version");
        return entry.group(1);
    }

    // Compares numeric versions part by part, e.g. 2.9.0 < 2.14.1
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
