# Day 04 hints — Unit Tests with JUnit 5, AssertJ and Mockito

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Mockito service tests

1. The service needs a repository. In a unit test it gets a fake one.
2. `@ExtendWith(MockitoExtension.class)`, `@Mock AccountRepository`, `@InjectMocks AccountService`; script it with `when(...).thenReturn(...)`.
3. For "never saved": `verify(accountRepository, never()).save(any())`. For a saved account: `when(repo.save(any(Account.class))).thenAnswer(call -> call.getArgument(0))`.

## Comparing money in assertions

1. `10.0` and `10.00` are different `BigDecimal`s to `equals`.
2. AssertJ has an assertion that uses `compareTo`.
3. `assertThat(balance).isEqualByComparingTo("10.00")`.
