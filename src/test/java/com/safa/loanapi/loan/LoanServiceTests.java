package com.safa.loanapi.loan;

import com.safa.loanapi.customer.CustomerRepository;
import com.safa.loanapi.customer.dao.Customer;
import com.safa.loanapi.exception.CustomerNotFoundException;
import com.safa.loanapi.exception.NotEnoughLimitException;
import com.safa.loanapi.installment.InstallmentRepository;
import com.safa.loanapi.installment.dao.Installment;
import com.safa.loanapi.loan.dao.Loan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTests {

    @InjectMocks
    private LoanService loanService;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private InstallmentRepository installmentRepository;

    @Mock
    private CustomerRepository customerRepository;

    public LoanServiceTests() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void CreateLoan_SuccessfulCreation() {
        long customerId = 1L;
        double amount = 1000.0;
        double rate = 0.1;
        int numOfInstallments = 6;

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername("admin");
        customer.setCreditLimit(BigDecimal.valueOf(5000.0));
        customer.setUsedCreditLimit(BigDecimal.valueOf(1000.0));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        Loan loan = new Loan(customerId, BigDecimal.valueOf(amount), BigDecimal.valueOf(1100.0), numOfInstallments, false);
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        Loan createdLoan = loanService.createLoan(customerId, amount, rate, numOfInstallments);

        assertNotNull(createdLoan);
        assertEquals(customerId, createdLoan.getCustomerId());
        assertEquals(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_EVEN), createdLoan.getAmount().setScale(2, RoundingMode.HALF_EVEN));
        assertEquals(BigDecimal.valueOf(1100.0).setScale(2, RoundingMode.HALF_EVEN), createdLoan.getTotalAmount().setScale(2, RoundingMode.HALF_EVEN));
        assertEquals(numOfInstallments, createdLoan.getNumberOfInstallments());
        assertFalse(createdLoan.getIsPaid());

        verify(customerRepository).save(customer);
        verify(installmentRepository, times(numOfInstallments)).save(any(Installment.class));
    }

    @Test
    public void CreateLoan_WhenCustomerNotFoundThenShouldReturnCustomerNotFoundException() {
        long customerId = 1L;
        double amount = 1000.0;
        double rate = 0.1;
        int numOfInstallments = 5;

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> loanService.createLoan(customerId, amount, rate, numOfInstallments));

        verify(customerRepository).findById(customerId);
        verifyNoInteractions(loanRepository, installmentRepository);
    }

    @Test
    public void CreateLoan_WhenCreditLimitNotEnoughThenShouldReturnNotEnoughLimitExceptionException() {
        long customerId = 1L;
        double amount = 5000.0;
        double rate = 0.1;
        int numOfInstallments = 5;

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername("user1");
        customer.setCreditLimit(BigDecimal.valueOf(4000.0));
        customer.setUsedCreditLimit(BigDecimal.valueOf(1000.0));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        assertThrows(NotEnoughLimitException.class, () -> loanService.createLoan(customerId, amount, rate, numOfInstallments));

        verify(customerRepository).findById(customerId);
        verifyNoInteractions(loanRepository, installmentRepository);
    }
}

class DivideEvenlyTests {
    @Test
    void testDivideEvenlyExactDivision() {
        double amount = 100.0;
        double divider = 4.0;

        List<Double> result = LoanService.divideEvenly(amount, divider);

        assertEquals(4, result.size());
        assertTrue(result.stream().allMatch(value -> value == 25.0));
    }

    @Test
    void testDivideEvenlyWithRemainder() {
        // Test case where the division leaves a remainder
        double amount = 100.0;
        double divider = 3.0;

        List<Double> result = LoanService.divideEvenly(amount, divider);

        assertEquals(3, result.size());
        assertEquals(33.33, result.get(0), 0.01);
        assertEquals(33.33, result.get(1), 0.01);
        assertEquals(33.34, result.get(2), 0.01);
    }

    @Test
    void testDivideEvenlySmallAmount() {
        // Test case with a very small amount
        double amount = 0.01;
        double divider = 3.0;

        List<Double> result = LoanService.divideEvenly(amount, divider);

        assertEquals(3, result.size());
        assertEquals(0.0, result.get(0), 0.01);
        assertEquals(0.0, result.get(1), 0.01);
        assertEquals(0.01, result.get(2), 0.01);
    }

    @Test
    void testDivideEvenlyZeroAmount() {
        // Test case with zero amount
        double amount = 0.0;
        double divider = 5.0;

        List<Double> result = LoanService.divideEvenly(amount, divider);

        assertEquals(5, result.size());
        assertTrue(result.stream().allMatch(value -> value == 0.0));
    }

    @Test
    void testDivideEvenlySingleDivider() {
        // Test case with a single divider
        double amount = 50.0;
        double divider = 1.0;

        List<Double> result = LoanService.divideEvenly(amount, divider);

        assertEquals(1, result.size());
        assertEquals(50.0, result.get(0));
    }

    @Test
    void testDivideEvenlyNegativeAmount() {
        // Test case with negative amount
        double amount = -100.0;
        double divider = 4.0;

        List<Double> result = LoanService.divideEvenly(amount, divider);

        assertEquals(4, result.size());
        assertTrue(result.stream().allMatch(value -> value == -25.0));
    }

    @Test
    void testDivideEvenlyInvalidDivider() {
        // Test case with invalid divider
        double amount = 100.0;
        double divider = 0.0;

        assertThrows(ArithmeticException.class, () -> LoanService.divideEvenly(amount, divider));
    }
}