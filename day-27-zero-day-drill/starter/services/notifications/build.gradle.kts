// The notifications service. Runs as its own application: ./gradlew :services:notifications:run
plugins {
    application
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

application {
    mainClass = "com.example.notifications.NotificationsApp"
}

dependencies {
    // Logs through the Log4j API, routed to SLF4J (the same set-up Spring Boot uses by default)
    implementation(libs.log4j.api)
    runtimeOnly(libs.log4j.to.slf4j)
    runtimeOnly(libs.slf4j.simple)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
