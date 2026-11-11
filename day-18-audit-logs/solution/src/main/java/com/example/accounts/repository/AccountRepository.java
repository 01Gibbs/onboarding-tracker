package com.example.accounts.repository;

import com.example.accounts.model.Account;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// No code needed: Spring Data writes findAll, findById, save, deleteById... for us.
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUsernameAndCurrency(String username, String currency); // ... WHERE username = ? AND currency = ?
}
