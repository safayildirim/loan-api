package com.safa.loanapi.customer;

import com.safa.loanapi.customer.dao.Customer;
import com.safa.loanapi.customer.dto.CreateCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Creates a new customer and saves it to the repository.
     *
     * <p>This method takes a {@link CreateCustomer} request object, encodes the customer's password using
     * {@link BCryptPasswordEncoder}, and constructs a new {@link Customer} entity with the provided details.
     * The customer is then saved to the database using the {@link CustomerRepository}.</p>
     *
     * @param req the {@link CreateCustomer} object containing the details of the customer to be created,
     *            including name, surname, username, password, role, and credit limit.
     * @return the newly created {@link Customer} entity after being saved in the database.
     */
    public Customer createCustomer(CreateCustomer req) {
        // Encode the customer's plain text password using BCrypt for security
        String encodedPassword = new BCryptPasswordEncoder().encode(req.getPassword());

        // Create a new Customer entity using the provided details and the encoded password
        Customer customer = new Customer(req.getName(), req.getSurname(), req.getUsername(), encodedPassword,
                req.getRole(), BigDecimal.valueOf(req.getCreditLimit()));

        // Save the customer entity to the database and return the saved entity
        return this.customerRepository.save(customer);
    }
}
