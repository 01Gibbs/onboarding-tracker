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
    // Log4j 2 does the actual logging: it writes the audit lines.
    // The version comes from gradle/libs.versions.toml, like every other library, so a security fix
    // is one change in one place. (It was a hard-coded 2.14.1: INC-2026-1111.)
    implementation(libs.log4j.core)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
