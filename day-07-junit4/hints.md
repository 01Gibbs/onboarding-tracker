# Day 07 hints — Read, Write and Migrate JUnit 4 Tests

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Writing JUnit 4 style

1. Copy the style of the tests already in the file.
2. JUnit 4: `org.junit.Test`, `Assert.assertEquals(expected, actual)`, `@Test(expected = …)` or JUnit 4.13's `Assert.assertThrows`.
3. `assertEquals("Account 99 not found", ex.getMessage())` after `AccountNotFoundException ex = assertThrows(AccountNotFoundException.class, () -> service.getById(99L));`

## Migrating without losing tests

1. Count the tests in the HTML report before you change anything.
2. `@RunWith(MockitoJUnitRunner.class)` → `@ExtendWith(MockitoExtension.class)`; `@Before` → `@BeforeEach`; `Assert` → AssertJ.
3. Remove the `junit-vintage-engine` dependency last, then check the count is still 9.
