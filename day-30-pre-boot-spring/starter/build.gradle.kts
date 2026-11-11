plugins {
    java
    war
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

val springVersion = "5.3.39"      // last open-source Spring Framework 5.3 release
val jacksonVersion = "2.13.5"

dependencies {
    implementation("org.springframework:spring-webmvc:$springVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    // The application server (Tomcat, WebLogic, WebSphere) provides the Servlet API at runtime
    providedCompile("javax.servlet:javax.servlet-api:4.0.1")

    testImplementation("org.springframework:spring-test:$springVersion")
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.jayway.jsonpath:json-path:2.7.0")
    testImplementation("org.hamcrest:hamcrest:2.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
