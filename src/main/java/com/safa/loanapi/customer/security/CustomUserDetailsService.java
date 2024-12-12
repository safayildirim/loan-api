package com.safa.loanapi.customer.security;

import com.safa.loanapi.customer.dao.Customer;
import com.safa.loanapi.customer.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Custom implementation of {@link UserDetailsService} for loading user-specific data.
     *
     * <p>This service retrieves a {@link Customer} entity from the database by username and converts it
     * into a {@link CustomUserDetails} object, which implements {@link UserDetails}. The method is used
     * by Spring Security for authentication and authorization processes.</p>
     *
     * @param username the username of the customer to be loaded.
     * @return a {@link CustomUserDetails} object containing the customer's authentication and
     *         authorization information.
     *
     * @throws UsernameNotFoundException if no customer with the specified username is found in the database.
     *
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new CustomUserDetails(
                customer.getId(),
                customer.getUsername(),
                customer.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + customer.getRole().name()))
        );
    }
}
