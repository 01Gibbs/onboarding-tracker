# Day 05 hints — Testing the Web Layer and the Repository

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Controller tests

1. You want HTTP, JSON and validation, but no database.
2. `@WebMvcTest(AccountController.class)` + `@Autowired MockMvc` + `@MockitoBean AccountService`.
3. `mockMvc.perform(get("/accounts/1")).andExpect(status().isOk()).andExpect(jsonPath("$.owner").value("Ada Lovelace"))`; for a 400, finish with `verifyNoInteractions(accountService)`.

## Repository tests

1. You want a real database, but only JPA.
2. `@DataJpaTest` gives a fresh in-memory H2 and rolls back each test.
3. `assertThat(accountRepository.count()).isZero()`; save, then `findById(saved.getId())` and `isPresent()`.
