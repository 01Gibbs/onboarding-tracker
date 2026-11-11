package com.example.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Checks the Dockerfile without Docker: it only reads two text files.
 *
 * <ul>
 *   <li>{@code Dockerfile} in the project folder (next to {@code build.gradle.kts})</li>
 *   <li>{@code build.gradle.kts}, for the Java toolchain version</li>
 * </ul>
 *
 * Gradle runs tests with the project folder as the working directory, so plain relative paths work.
 */
class DockerfileTest {

    private static final Path DOCKERFILE = Path.of("Dockerfile");
    private static final Path BUILD_FILE = Path.of("build.gradle.kts");

    /** One Dockerfile instruction, e.g. keyword "FROM", arguments "eclipse-temurin:21-jre AS run". */
    record Instruction(String keyword, String arguments) {
    }

    @Test
    void finalImageJavaVersionMatchesTheToolchain() throws IOException {
        int toolchain = toolchainJavaVersion();
        String image = imageName(lastFrom(instructions()));
        String expected = "Images that count: eclipse-temurin with a tag that starts with the toolchain's Java version,"
                + " e.g. eclipse-temurin:" + toolchain + "-jre-<os release>";

        assertThat(repositoryOf(image))
                .as("The final base image '%s' must be a Java runtime image. %s", image, expected)
                .isEqualTo("eclipse-temurin");
        Matcher major = Pattern.compile("^(\\d+)").matcher(tagOf(image));
        if (!major.find()) {
            fail("Can't tell the Java version of '" + image + "'. " + expected);
        }
        assertThat(Integer.parseInt(major.group(1)))
                .as("Java version in the final base image '%s' must equal the toolchain in build.gradle.kts (%d). %s",
                        image, toolchain, expected)
                .isEqualTo(toolchain);
    }

    @Test
    void noStageUsesTheDeprecatedOpenjdkImage() throws IOException {
        for (Instruction from : froms(instructions())) {
            String image = imageName(from);
            assertThat(repositoryOf(image))
                    .as("'%s': the official 'openjdk' image is deprecated (see its Docker Hub page). Use a supported one,"
                            + " e.g. eclipse-temurin", image)
                    .isNotEqualTo("openjdk");
        }
    }

    @Test
    void finalStageRunsAsANonRootUser() throws IOException {
        List<Instruction> all = instructions();
        List<Instruction> finalStage = all.subList(all.lastIndexOf(lastFrom(all)), all.size());

        // Docker runs the container as the last USER of the final stage, wherever it appears
        String user = null;
        for (Instruction instruction : finalStage) {
            if (instruction.keyword().equals("USER")) {
                user = instruction.arguments();
            }
        }
        if (user == null) {
            fail("No USER instruction in the final stage, so the app runs as root");
        }
        String name = user.split(":")[0].trim();
        assertThat(name).as("The final stage's last USER is %s", user).isNotIn("root", "0");
    }

    // ---- simple Dockerfile parsing ------------------------------------------------------------

    /** Instructions in order, with comments and blank lines dropped and "\" continuation lines joined. */
    private static List<Instruction> instructions() throws IOException {
        assertThat(DOCKERFILE).as("Dockerfile in the project folder").exists();
        List<Instruction> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String raw : Files.readAllLines(DOCKERFILE)) {
            String line = raw.strip();
            if (line.startsWith("#") || (line.isEmpty() && current.isEmpty())) {
                continue;
            }
            if (line.endsWith("\\")) {
                current.append(line, 0, line.length() - 1).append(' ');
                continue;
            }
            current.append(line);
            String text = current.toString().strip();
            current.setLength(0);
            if (text.isEmpty()) {
                continue;
            }
            String[] parts = text.split("\\s+", 2);
            result.add(new Instruction(parts[0].toUpperCase(Locale.ROOT), parts.length > 1 ? parts[1].strip() : ""));
        }
        return result;
    }

    private static List<Instruction> froms(List<Instruction> all) {
        List<Instruction> froms = all.stream().filter(i -> i.keyword().equals("FROM")).toList();
        assertThat(froms).as("FROM instructions in the Dockerfile").isNotEmpty();
        return froms;
    }

    private static Instruction lastFrom(List<Instruction> all) {
        List<Instruction> froms = froms(all);
        return froms.get(froms.size() - 1);
    }

    /** "FROM --platform=x eclipse-temurin:21-jre AS run" gives "eclipse-temurin:21-jre". */
    private static String imageName(Instruction from) {
        for (String word : from.arguments().split("\\s+")) {
            if (!word.startsWith("--")) {
                return word;
            }
        }
        throw new AssertionError("FROM without an image: " + from);
    }

    /** "docker.io/library/eclipse-temurin:21-jre@sha256:..." gives "21-jre"; no tag gives "". */
    private static String tagOf(String image) {
        String withoutDigest = image.split("@")[0];
        String lastSegment = withoutDigest.substring(withoutDigest.lastIndexOf('/') + 1);
        int colon = lastSegment.indexOf(':');
        return colon < 0 ? "" : lastSegment.substring(colon + 1);
    }

    /** "docker.io/library/openjdk:11-jre-slim" gives "openjdk". */
    private static String repositoryOf(String image) {
        String withoutDigest = image.split("@")[0];
        String lastSegment = withoutDigest.substring(withoutDigest.lastIndexOf('/') + 1);
        return lastSegment.split(":")[0].toLowerCase(Locale.ROOT);
    }

    private static int toolchainJavaVersion() throws IOException {
        Matcher matcher = Pattern.compile("JavaLanguageVersion\\.of\\((\\d+)\\)").matcher(Files.readString(BUILD_FILE));
        assertThat(matcher.find()).as("JavaLanguageVersion.of(...) in build.gradle.kts").isTrue();
        return Integer.parseInt(matcher.group(1));
    }
}
