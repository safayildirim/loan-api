package com.safa.loanapi.customer;

import com.safa.loanapi.customer.dto.CreateCustomer;
import com.safa.loanapi.customer.dao.Customer;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/customers")
    ResponseEntity<Customer> createCustomer(@Valid @RequestBody CreateCustomer req) {
        return ResponseEntity.ok(this.customerService.createCustomer(req));
    }
}
