package com.example.accounts.service;

import com.example.accounts.repository.AccountRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    AccountRepository accountRepository;

    @InjectMocks
    AccountService accountService;

    // TODO: getByIdReturnsTheAccountWhenItExists — returns the account the repository finds
    // TODO: getByIdThrowsWhenTheAccountIsMissing — AccountNotFoundException, message "Account 99 not found"
    // TODO: createSavesTheNewAccount — owner and balance of the saved account
    // TODO: createRejectsAnUnsupportedCurrency — UnsupportedCurrencyException, and save is never called
}
