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
}
