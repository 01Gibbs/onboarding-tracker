# Day 30 hints — Move a Pre-Boot Spring App onto Spring Boot

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## A Boot build

1. Boot comes in as Gradle plugins; then starters replace the hand-picked Spring jars.
2. Day 6's `build.gradle.kts` is a Boot 2.7.18 build. Compare its `plugins` and `dependencies` with today's.
3. Plugins `org.springframework.boot` 2.7.18 and `io.spring.dependency-management`; `spring-boot-starter-web` replaces `spring-webmvc`, Jackson and the Servlet API. Then run `dependencyInsight --dependency spring-webmvc` and compare with 5.3.39.

## From XML to Java

1. Read each XML element and ask what Boot already does for you.
2. The brief's "What's in the starter" table, and Boot 2.7's "Importing XML Configuration" section.
3. `AccountsApplication` with `@SpringBootApplication` in `com.example.accounts` (so the scan covers `controller`); a `@Configuration` class with `@Bean AccountService accountService(@Value("${accounts.currency}") String currency)`; `accounts.currency` moves to `application.properties`; delete `src/main/webapp`.

## The old tests fail now

1. `AccountControllerTest` still points at XML files you deleted.
2. Day 5's `@WebMvcTest` and Day 14's `@Import`.
3. `@WebMvcTest(AccountController.class)` + `@Import(<your config class>)` + `@Autowired MockMvc`; the test methods stay the same.
