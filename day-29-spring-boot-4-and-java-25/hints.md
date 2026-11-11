# Day 29 hints — The Next Hop: Spring Boot 4 on Java 25

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Get it to compile

1. Change one thing, run `./gradlew build`, read the *first* error. Write it down before you fix it.
2. "not part of the dependency lock state" isn't a code problem: it's Day 8's lockfile doing its job. For missing packages, look at the guide's "Test Code" section and the starters table.
3. Classes like `WebMvcTest` and `TestRestTemplate` now live under `org.springframework.boot.<technology>…` in their own jars. Compare with `day-23-mask-what-you-show/solution/build.gradle.kts` and its test imports, and search the guide for "TestRestTemplate".

## Make the tests pass

1. If a test can't find a bean it used to get for free, ask what `@SpringBootTest` stopped providing in Boot 4.
2. The guide's "Upgrading Testing Features" section covers MockMvc and `TestRestTemplate` with `@SpringBootTest`.
3. There's an `@AutoConfigure…` annotation for each of them; one of our tests already has the MockMvc one.

## Green isn't done

1. Check what you actually got, not what you asked for: the Tomcat version, and every feature the app had before (including in the `dev` profile).
2. `./gradlew dependencyInsight --dependency tomcat-embed-core --configuration runtimeClasspath`, then read the selection reasons. For the dev profile: run it and try the URL from `application-dev.properties`.
3. Something in `build.gradle.kts` from Day 12 is now holding a library *back*. And a feature that used to come with the web starter now needs its own module: the modern days' build file lists it.
