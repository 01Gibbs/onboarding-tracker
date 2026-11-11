# Accounts service image.
# Build the jar first, then the image (--pull fetches the latest patched base image):
#   ./gradlew bootJar
#   docker build --pull -t accounts .
#
# Base image: Eclipse Temurin (the maintained successor to the deprecated "openjdk" image),
# JRE only, Java 21 to match the toolchain in build.gradle.kts, on Ubuntu 26.04 LTS ("resolute").
# Tags: https://hub.docker.com/_/eclipse-temurin
# The tag moves when Temurin or Ubuntu ship a patch, so rebuilding picks up fixes.
FROM eclipse-temurin:21-jre-resolute

# A dedicated user with no login shell and no home folder. /app/data is where H2 writes
# (spring.datasource.url=jdbc:h2:file:./data/accounts), so that user must own it.
RUN groupadd --system --gid 10001 app \
 && useradd --system --no-log-init --uid 10001 --gid app --no-create-home --shell /usr/sbin/nologin app \
 && mkdir -p /app/data \
 && chown app:app /app/data

WORKDIR /app

# The jar stays owned by root, so the app can't change its own code.
COPY build/libs/accounts-0.0.1-SNAPSHOT.jar app.jar

# Numeric, so Kubernetes' runAsNonRoot can check it without reading /etc/passwd.
USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
