plugins {
    java
    id("org.springframework.boot") version "2.7.18"   // the Boot line built on Spring Framework 5.3
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

// Boot 2.7.18 manages Spring Framework 5.3.31, but the WAR was already on 5.3.39 (the last open-source
// 5.3 release). Don't go backwards. Remove this when moving to Boot 3.
extra["spring-framework.version"] = "5.3.39"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")   // Spring MVC, Jackson, embedded Tomcat
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
