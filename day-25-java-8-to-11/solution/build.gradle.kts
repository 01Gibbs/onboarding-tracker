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

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // JAXB left the JDK in Java 11 (JEP 320). Add it back as ordinary libraries,
    // at the versions Boot 2.7 manages: the API (still the javax.xml.bind package in 2.3.x)...
    implementation("jakarta.xml.bind:jakarta.xml.bind-api")
    // ...and an implementation, which only needs to be there at run time.
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
