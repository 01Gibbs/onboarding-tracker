// The root project has no code of its own. Each folder under libs/ and services/ is a project
// with its own build.gradle.kts (the list is in settings.gradle.kts). Run a task for one project
// with its path, e.g.
//   ./gradlew :services:payments:test
// or for every project at once by name, e.g.
//   ./gradlew test
// Each project's test results are in its own build/test-results/test folder.
