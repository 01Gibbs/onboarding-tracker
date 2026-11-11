package com.example.accounts.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.example.accounts.exception.AccountNotFoundException;
import com.example.accounts.model.Account;
import com.example.accounts.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

// Team convention (2022): this class stays JUnit 4 until we migrate it.
@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    @Before
    public void setUp() {
        accountService = new AccountService(accountRepository);
    }

    @Test
    public void getByIdReturnsTheAccount() {
        Account ada = new Account("Ada Lovelace", "GBP", new BigDecimal("1250.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(ada));

        Account found = accountService.getById(1L);

        assertEquals("Ada Lovelace", found.getOwner());
    }

    @Test(expected = AccountNotFoundException.class)
    public void getByIdThrowsWhenTheAccountIsMissing() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        accountService.getById(99L);
    }

    // TODO 1: findAllReturnsEveryAccount
    //   Stub accountRepository.findAll() to return two accounts (Arrays.asList(...)),
    //   then assertEquals that accountService.findAll() has 2 of them.

    // TODO 2: createCopiesTheRequestIntoANewAccount
    //   Stub accountRepository.save(any(Account.class)) to hand back its argument:
    //   .thenAnswer(call -> call.getArgument(0)). Build a CreateAccountRequest with its setters,
    //   call accountService.create(request) and check the owner and currency.

    // TODO 3: missingAccountMessageNamesTheId
    //   @Test(expected = ...) can't check the message. Use org.junit.Assert.assertThrows
    //   (JUnit 4.13+) to catch the AccountNotFoundException for id 99,
    //   then assertEquals "Account 99 not found" against its getMessage().

    // All three stay JUnit 4: public void methods, org.junit.Test, org.junit.Assert.
    // Remember: assertEquals(expected, actual). Expected comes first.
}
