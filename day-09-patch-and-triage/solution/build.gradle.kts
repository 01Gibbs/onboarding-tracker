plugins {
    java
    id("org.springframework.boot") version "2.7.18"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    mavenCentral()
}

// Security overrides of versions managed by Spring Boot 2.7.18.
// Remove each one when we move to a Boot version that manages this version (or newer) itself.
extra["snakeyaml.version"] = "2.2"                // CVE-2022-1471 and friends
extra["logback.version"] = "1.2.13"               // CVE-2023-6378
extra["tomcat.version"] = "9.0.123"               // latest Tomcat 9.0.x patch
extra["spring-framework.version"] = "5.3.39"      // last open-source 5.3.x
extra["spring-security.version"] = "5.7.13"       // CVE-2024-38821

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

// Record the exact version of every library in gradle.lockfile, so scanners can read it.
// Regenerate after any dependency change: ./gradlew dependencies --write-locks
dependencyLocking {
    lockAllConfigurations()
}

tasks.withType<Test> {
    useJUnitPlatform()
}
