# Day 25 hints — From Java 8 to 11

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Why did it break?

1. Read the first compile error, not the last one. Which package is missing?
2. That package used to be part of the JDK. Search for "JEP 320" and read its list of removed modules.
3. `java.xml.bind` (JAXB) is in that list. The fix is to supply it yourself as dependencies, as JEP 320's "Risks and Assumptions" section suggests.

## Which coordinates and versions?

1. Don't pick a version from a search engine: the framework already chose one.
2. Spring Boot 2.7.18's "Dependency Versions" appendix lists every JAXB-related artifact it manages; look for `xml.bind` and `jaxb`.
3. You need two: `jakarta.xml.bind:jakarta.xml.bind-api` (2.3.3: the "jakarta" coordinates, but still the `javax.xml.bind` package) and an implementation, `org.glassfish.jaxb:jaxb-runtime` (2.3.9). Leave the versions out.

## It compiles but the tests fail

1. Read the exception in `build/reports/tests/test/index.html`. Is it a compile problem or a run-time one?
2. `Implementation of JAXB-API has not been found` means the API is there but nothing implements it.
3. Add the implementation with `runtimeOnly(...)`. Your code never imports it, so it doesn't belong on the compile classpath.
