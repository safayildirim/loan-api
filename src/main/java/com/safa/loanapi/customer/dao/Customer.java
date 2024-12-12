package com.safa.loanapi.customer.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String surname;
    private String username;
    private String password;
    @Column(precision = 10, scale = 2)
    private BigDecimal creditLimit;
    @Column(precision = 10, scale = 2)
    private BigDecimal usedCreditLimit = BigDecimal.valueOf(0.0);

    @Enumerated(EnumType.STRING)
    private Role role;

    public Customer(String name, String surname, String username, String password, Role role, BigDecimal creditLimit) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.password = password;
        this.role = role;
        this.creditLimit = creditLimit;
    }

    public enum Role {
        ADMIN, CUSTOMER
    }
}
