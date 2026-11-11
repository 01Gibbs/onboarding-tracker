// Shared audit logging. Written by the platform team in 2021; several services use it.
plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Log4j 2 does the actual logging: it writes the audit lines
    implementation("org.apache.logging.log4j:log4j-core:2.14.1")

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
