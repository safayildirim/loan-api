package com.safa.loanapi.customer.dto;

import com.safa.loanapi.customer.dao.Customer;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCustomer {
    @NotBlank
    private String name;
    @NotBlank
    private String surname;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private Customer.Role role;
    private double creditLimit;
}
