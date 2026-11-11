# Day 24 hints — Maven for Gradle Users

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Find Jackson in the tree

1. Maven's equivalent of `dependencies` is a goal of the dependency plugin.
2. `./mvnw dependency:tree` prints everything; the Maven docs page for `dependency:tree` lists a parameter that filters by group or artifact.
3. `./mvnw dependency:tree -Dincludes=tools.jackson.core` (Jackson 3's group id is `tools.jackson.core`, not `com.fasterxml.jackson.core`). Read the path from the top: which of *your* dependencies is first in the chain?

## Which property?

1. Boot doesn't manage every Jackson module one by one; it imports Jackson's own BOM.
2. Spring Boot's "Version Properties" appendix lists every property it reads; or search the effective POM (`./mvnw help:effective-pom -Doutput=target/effective-pom.xml`) for `jackson`.
3. The property is named after the BOM, not after `jackson-databind`. There is no `jackson-databind.version`.

## Override it the Maven way

1. In Gradle you set `extra["…"]`. Maven's equivalent is a block you already have in `pom.xml`.
2. Look at how `<java.version>` is set. Boot's docs show the same trick with `<slf4j.version>`.
3. Add one entry next to `<java.version>` in `<properties>`, then re-run `dependency:tree -Dincludes=tools.jackson.core`: both `jackson-databind` and `jackson-core` should change.
