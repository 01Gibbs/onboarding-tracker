# Written in 2022, when this service was Java 11 / Spring Boot 2.7.
# Build the jar first, then the image:
#   ./gradlew bootJar
#   docker build -t accounts .
FROM openjdk:11-jre-slim

EXPOSE 8080

COPY build/libs/accounts-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
