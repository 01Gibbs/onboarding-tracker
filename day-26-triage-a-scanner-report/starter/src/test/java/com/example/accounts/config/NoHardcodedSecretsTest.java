package com.example.accounts.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

// SAST-101 / SAST-106: secrets come from configuration (an environment variable or a vault), never from the code.
class NoHardcodedSecretsTest {

    // A name that sounds secret, given a string literal: API_KEY = "...", password = "...", clientSecret = "..."
    static final Pattern SECRET_LITERAL = Pattern.compile(
            "(?i)\\w*(key|secret|password|token)\\w*\\s*=\\s*\"[^\"]{8,}\"");
    // The live key's prefix, wherever it appears
    static final Pattern LIVE_KEY = Pattern.compile("ntf_live_\\w+");

    @Test
    void noSecretLiteralsInMainCode() throws IOException {
        try (Stream<Path> files = Files.walk(Path.of("src/main"))) {
            List<String> offenders = files.filter(Files::isRegularFile)
                    .filter(path -> {
                        String text = read(path);
                        return SECRET_LITERAL.matcher(text).find() || LIVE_KEY.matcher(text).find();
                    })
                    .map(Path::toString)
                    .toList();

            assertThat(offenders).as("files under src/main with a secret written into them").isEmpty();
        }
    }

    @Test
    void theApiKeyComesFromConfiguration() {
        new ApplicationContextRunner()
                .withPropertyValues("notifications.api-key=key-from-configuration")
                .withUserConfiguration(NotificationsClient.class)
                .run(context -> assertThat(context.getBean(NotificationsClient.class).authorizationHeader())
                        .isEqualTo("Bearer key-from-configuration"));
    }

    static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
