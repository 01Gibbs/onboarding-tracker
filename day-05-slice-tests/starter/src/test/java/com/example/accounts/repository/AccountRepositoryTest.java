package com.example.accounts.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    AccountRepository accountRepository;

    // TODO: startsEmpty — a fresh test database has 0 accounts
    // TODO: savesAndFindsAnAccount — a saved account gets an id and can be found again
}
