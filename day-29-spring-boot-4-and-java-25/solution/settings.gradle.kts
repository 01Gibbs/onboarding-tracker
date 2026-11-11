plugins {
    // Lets Gradle download the JDK 25 toolchain if this machine doesn't have one
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "accounts"
