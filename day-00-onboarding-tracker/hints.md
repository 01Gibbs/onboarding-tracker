# Day 00 hints — Build an Onboarding Tracker REST API

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer; `bruno/` holds the brief's checks.

## Creating the project

1. start.spring.io generates it. Which build tool and dependencies does the brief ask for?
2. Project: **Gradle - Kotlin**, Java 25, dependencies: Spring Web, Spring Data JPA, Validation, H2 Database.
3. Unzip it, `cd` into the folder that contains `gradlew`, then `./gradlew bootRun`. If you see `no such file or directory: ./gradlew`, you're one folder too high or low.

## The employee table

1. JPA turns one annotated class into one table.
2. `@Entity` on the class, `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)` on a `Long id`, and a protected no-argument constructor.
3. Use `java.time.LocalDate` for `onboardingDate`, not `String` and not `java.util.Date`. A `JpaRepository<Employee, Long>` interface gives you `save` and `findAll` for free.

## POST returns 201 with the saved employee

1. A plain return value is a 200. What lets you choose the status code?
2. `ResponseEntity`. It has a method for "created".
3. `return ResponseEntity.created(URI.create("/api/employees/" + saved.getId())).body(saved);`

## Missing or blank fields must be 400

1. Rules go on the request object, but nothing checks them unless you ask.
2. `@NotBlank` on `name` and `role`, `@NotNull` on `onboardingDate`, and one more annotation on the controller parameter.
3. `create(@Valid @RequestBody CreateEmployeeRequest request)`. Without `@Valid`, a missing name reaches the database and fails as a 500.

## `"10-11-2026"` must be 400

1. If `onboardingDate` is a `LocalDate`, who parses the JSON string?
2. Jackson parses `yyyy-MM-dd` into a `LocalDate` and rejects anything else before your code runs.
3. With `LocalDate` this is already a 400. If you used `String`, switch to `LocalDate` rather than writing a regex (a regex would accept `2026-02-30`).

## GET returns `[]` when nothing is saved

1. What does `findAll()` return for an empty table?
2. An empty `List`, and Spring writes an empty list as JSON.
3. `@GetMapping public List<Employee> getAll() { return employeeRepository.findAll(); }`, and nothing special is needed.
