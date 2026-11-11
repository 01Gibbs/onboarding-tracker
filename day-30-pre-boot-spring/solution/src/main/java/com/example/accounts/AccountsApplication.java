package com.example.accounts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Replaces web.xml: component scanning (was <context:component-scan>), Spring MVC and Jackson
// (was <mvc:annotation-driven/>) and an embedded Tomcat (was the application server).
@SpringBootApplication
public class AccountsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountsApplication.class, args);
    }
}
