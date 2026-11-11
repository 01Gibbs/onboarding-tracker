package com.example.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.accounts.dto.CreateAccountRequest;
import com.example.accounts.exception.AccountNotFoundException;
import com.example.accounts.model.Account;
import com.example.accounts.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository);
    }

    @Test
    void getByIdReturnsTheAccount() {
        Account ada = new Account("Ada Lovelace", "GBP", new BigDecimal("1250.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(ada));

        Account found = accountService.getById(1L);

        assertThat(found.getOwner()).isEqualTo("Ada Lovelace");
    }

    @Test
    void getByIdThrowsWhenTheAccountIsMissing() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getById(99L))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void findAllReturnsEveryAccount() {
        List<Account> accounts = Arrays.asList(
                new Account("Ada Lovelace", "GBP", new BigDecimal("1250.00")),
                new Account("Alan Turing", "EUR", new BigDecimal("80.50")));
        when(accountRepository.findAll()).thenReturn(accounts);

        List<Account> found = accountService.findAll();

        assertThat(found).hasSize(2);
    }

    @Test
    void createCopiesTheRequestIntoANewAccount() {
        // "When save is called, hand back whatever was passed in"
        when(accountRepository.save(any(Account.class))).thenAnswer(call -> call.getArgument(0));
        CreateAccountRequest request = new CreateAccountRequest();
        request.setOwner("Grace Hopper");
        request.setCurrency("USD");
        request.setOpeningBalance(new BigDecimal("10.00"));

        Account created = accountService.create(request);

        assertThat(created.getOwner()).isEqualTo("Grace Hopper");
        assertThat(created.getCurrency()).isEqualTo("USD");
    }

    @Test
    void missingAccountMessageNamesTheId() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getById(99L))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account 99 not found");
    }
}
