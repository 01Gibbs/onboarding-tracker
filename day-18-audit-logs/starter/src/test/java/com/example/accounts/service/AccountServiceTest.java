package com.example.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.accounts.dto.CreateAccountRequest;
import com.example.accounts.exception.AccountNotFoundException;
import com.example.accounts.exception.UnsupportedCurrencyException;
import com.example.accounts.model.Account;
import com.example.accounts.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    AccountRepository accountRepository; // a fake repository: no database involved

    @InjectMocks
    AccountService accountService; // the real service, given the fake repository

    @Test
    void getByIdReturnsTheAccountWhenItExists() {
        Account account = new Account("ada", "Ada Lovelace", "GBP", new BigDecimal("10.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Account result = accountService.getById(1L);

        assertThat(result).isSameAs(account);
    }

    @Test
    void getByIdThrowsWhenTheAccountIsMissing() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getById(99L))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account 99 not found");
    }

    @Test
    void customersCanSeeTheirOwnAccount() {
        Account adas = new Account("ada", "Ada Lovelace", "GBP", new BigDecimal("10.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(adas));

        assertThat(accountService.getVisibleTo(1L, "ada", false)).isSameAs(adas);
    }

    @Test
    void customersCannotSeeSomeoneElsesAccount() {
        Account graces = new Account("grace", "Grace Hopper", "USD", new BigDecimal("10.00"));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(graces));

        assertThatThrownBy(() -> accountService.getVisibleTo(2L, "ada", false))
                .isInstanceOf(AccountNotFoundException.class);
    }

    // TODO: lookingAtSomeoneElsesAccountIsLogged — "ACCESS_DENIED user=ada account=2"

    @Test
    void tellersCanSeeAnyAccount() {
        Account graces = new Account("grace", "Grace Hopper", "USD", new BigDecimal("10.00"));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(graces));

        assertThat(accountService.getVisibleTo(2L, "teller", true)).isSameAs(graces);
    }

    @Test
    void createSavesTheNewAccount() {
        // "When save is called, hand back whatever was passed in"
        when(accountRepository.save(any(Account.class))).thenAnswer(call -> call.getArgument(0));

        Account created = accountService.create(
                new CreateAccountRequest("ada", "Ada Lovelace", "GBP", new BigDecimal("10.00")));

        assertThat(created.getOwner()).isEqualTo("Ada Lovelace");
        assertThat(created.getBalance()).isEqualByComparingTo("10.00");
    }

    @Test
    void createRejectsAnUnsupportedCurrency() {
        CreateAccountRequest request = new CreateAccountRequest("ada", "Ada Lovelace", "XYZ", new BigDecimal("10.00"));

        assertThatThrownBy(() -> accountService.create(request))
                .isInstanceOf(UnsupportedCurrencyException.class);
        verify(accountRepository, never()).save(any());
    }
}
