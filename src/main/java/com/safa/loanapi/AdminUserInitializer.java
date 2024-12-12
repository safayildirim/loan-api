package com.safa.loanapi;

import com.safa.loanapi.customer.dao.Customer;
import com.safa.loanapi.customer.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        Customer admin = new Customer();
        admin.setName("admin");
        admin.setSurname("admin");
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("12345"));
        admin.setRole(Customer.Role.ADMIN);
        customerRepository.save(admin);
        System.out.println("Admin user created with username: admin, password: 12345!");
    }
}
