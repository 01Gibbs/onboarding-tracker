# Day 30 afternoon — Move a Pre-Boot Spring App onto Spring Boot

> **In plain English:** Some of our older services were built with Spring before Spring Boot existed. They're wired together with XML files and have to be installed on a separate application server, which makes them slow to change and hard to upgrade. Today we move one onto Spring Boot, so it starts on its own and can follow the same upgrade path as our other services.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

What changes today: **the whole (pre-Boot) app's wiring and packaging**.

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-30-pre-boot-spring/starter day-30-pre-boot-spring/solution
```

## Agenda (25 min)

| Time | What |
|---|---|
| 0–3 | Recap the question. Show of hands: who got it green? Where did people get stuck in the chat today? |
| 3–14 | Solution tour (below), in order |
| 14–19 | Points to land, and common mistakes |
| 19–22 | Questions for the room |
| 22–25 | In our code, then tomorrow's question |

## Solution tour

1. **`starter/src/main/webapp/WEB-INF/`**: `web.xml` starts two contexts (root and dispatcher); `applicationContext.xml`, `dispatcher-servlet.xml`, `weblogic.xml`. For each element, ask what Boot replaces it with: `@SpringBootApplication` + `main` for `web.xml`, component scan for `<context:component-scan>`, auto-configuration for `<mvc:annotation-driven/>`, `application.properties` for the placeholder configurer, `server.servlet.context-path` for `weblogic.xml`'s context root.
2. **`starter/…/BootApplicationTest`**: Why it looks classes up by name (a red test must still compile before Boot is on the classpath), and how it turned green.
3. **`solution/build.gradle.kts`**: The Boot plugin, one starter, and `extra["spring-framework.version"] = "5.3.39"` with its comment. Show `dependencyInsight` without the override: 5.3.31, and `SpringVersionTest` goes red. Moving to Boot must not downgrade Spring.
4. **`solution` `AccountsApplication` and `config/AccountsConfig`**: Each line says which XML it replaced. `AccountService` and `AccountController` didn't change at all.
5. **The `solution` tests**: `AccountControllerTest` is now a `@WebMvcTest`; `BootApplicationTest` is a real `@SpringBootTest(webEnvironment = RANDOM_PORT)`.
6. **`./gradlew bootRun` and the Bruno collection**: The same contract, now on an embedded Tomcat.

## Points to land

- Change the wiring, not the behaviour: the same Framework line (5.3), the same contract, the same test assertions. That's why Boot 2.7 comes first, even though it's out of open-source support.
- `@ImportResource` is a legitimate first step on a big app (move the XML out of `WEB-INF`, which isn't on Boot's classpath); convert XML in small, tested steps afterwards. Leave `<mvc:annotation-driven/>` out of the import.
- Dependency management can move versions both ways: check what you actually resolve after a migration.
- Anything server-specific (`weblogic.xml`, JNDI, context root, shared libraries) is a conversation with the platform team, not just a delete.
- Boot 2.7 itself no longer gets free security fixes, so today is a first hop, not the end. This app's next hops are the ones the legacy service took: Day 11 (Boot 3), Day 13 (Java 21), Day 29 (Boot 4 on Java 25).

## Common mistakes

- Putting `@SpringBootApplication` in a sub-package (e.g. `config`): the scan misses `controller`, and `/accounts` is 404.
- Keeping `<mvc:annotation-driven/>` via `@ImportResource` (a second, differently configured MVC set-up), or adding `@EnableWebMvc` (Boot's MVC auto-configuration backs off, so Jackson settings and error handling change).
- Deleting `web.xml` but leaving `src/main/webapp` with the other XML files, or keeping the `war` plugin with no `SpringBootServletInitializer`: it builds, but the WAR won't start on a server.
- Letting Boot 2.7.18 quietly downgrade Spring Framework to 5.3.31.

## Questions for the room

- **Why not jump straight to Boot 3?**
  <details><summary>Answer</summary>That's three changes at once (wiring, `javax` → `jakarta`, Framework 5 → 6). One at a time keeps every step testable, and Day 11's OpenRewrite recipes expect a Boot app as input.</details>
- **The app gets its DataSource from WebLogic through JNDI. What do you do?**
  <details><summary>Answer</summary>Find out who owns the credentials and connection settings. In Boot it becomes `spring.datasource.*` (from environment variables or a secret store, Day 10), or keep `spring.datasource.jndi-name` while it's still deployed as a WAR.</details>

## In our code (2 minutes)

Prepare before the session: find any `web.xml`, `*-servlet.xml` or `weblogic.xml` (or WebSphere's `ibm-web-*.xml`) in our repositories. For one hit: which Spring version does it use, how is it deployed, and which of today's steps would it need? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

That's the course. This week, pick a real ticket from the team's backlog that looks like one of these days (a scanner finding, a dependency or JDK upgrade, an old app's build) and work it the same way: inspect, check primary sources, change one thing, prove it, and write down what you found.
