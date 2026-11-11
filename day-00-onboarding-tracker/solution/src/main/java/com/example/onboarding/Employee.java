package com.example.onboarding;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private LocalDate onboardingDate;

    // JPA needs a no-argument constructor
    protected Employee() {
    }

    public Employee(String name, String role, LocalDate onboardingDate) {
        this.name = name;
        this.role = role;
        this.onboardingDate = onboardingDate;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public LocalDate getOnboardingDate() {
        return onboardingDate;
    }
}
