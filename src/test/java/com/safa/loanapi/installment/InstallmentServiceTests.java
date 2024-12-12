package com.safa.loanapi.installment;

import com.safa.loanapi.WithMockCustomUser;
import com.safa.loanapi.customer.CustomerRepository;
import com.safa.loanapi.customer.dao.Customer;
import com.safa.loanapi.exception.CustomerNotFoundException;
import com.safa.loanapi.exception.LoanNotFoundException;
import com.safa.loanapi.installment.dao.Installment;
import com.safa.loanapi.loan.LoanRepository;
import com.safa.loanapi.loan.dao.Loan;
import com.safa.loanapi.loan.dto.LoanPaymentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableMethodSecurity
public class InstallmentServiceTests {

    @Autowired
    private InstallmentService installmentService;

    @MockBean
    private LoanRepository loanRepository;

    @MockBean
    private InstallmentRepository installmentRepository;

    @MockBean
    private CustomerRepository customerRepository;

    @BeforeEach
    public void setup() {
    }

    @Test
    @WithMockCustomUser(id = 100L)
    public void PayLoan_WhenThereIsEnoughAmountSuppliedThenPaymentShouldBeProcessed() {
        Long loanId = 1L;
        Long customerID = 100L;
        double paymentAmount = 300.0;

        Loan loan = new Loan();
        loan.setCustomerId(customerID);
        loan.setId(loanId);
        loan.setIsPaid(false);

        Installment installment1 = new Installment();
        installment1.setId(1L);
        installment1.setLoanId(loanId);
        installment1.setAmount(BigDecimal.valueOf(100.00));
        installment1.setTotalAmount(BigDecimal.valueOf(105.00));
        installment1.setDueDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));
        installment1.setIsPaid(false);

        Installment installment2 = new Installment();
        installment2.setId(2L);
        installment2.setLoanId(loanId);
        installment2.setAmount(BigDecimal.valueOf(100.00));
        installment2.setTotalAmount(BigDecimal.valueOf(105.00));
        installment2.setDueDate(LocalDate.now().plusMonths(2).withDayOfMonth(1));
        installment2.setIsPaid(false);

        Installment installment3 = new Installment();
        installment3.setId(3L);
        installment3.setLoanId(loanId);
        installment3.setAmount(BigDecimal.valueOf(100.00));
        installment3.setTotalAmount(BigDecimal.valueOf(105.00));
        installment3.setDueDate(LocalDate.now().plusMonths(3).withDayOfMonth(1));
        installment3.setIsPaid(false);

        List<Installment> installments = new ArrayList<>();
        installments.add(installment1);
        installments.add(installment2);
        installments.add(installment3);

        loan.setInstallments(installments);

        Customer customer = new Customer();
        customer.setId(customerID);
        customer.setUsername("customer1");

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(customerRepository.findById(customerID)).thenReturn(Optional.of(customer));

        List<Installment> unpaidInstallments = new ArrayList<>();
        Installment unpaidInstallment = new Installment();
        unpaidInstallment.setId(4L);
        unpaidInstallments.add(unpaidInstallment);
        when(installmentRepository.findAllByLoanIdAndIsPaidOrderByDueDateAsc(loanId, false)).thenReturn(unpaidInstallments);

        LoanPaymentInfo info = installmentService.payInstallment(customerID, loanId, paymentAmount);

        assertEquals(3, info.getPaidInstallments().size());
        assertFalse(info.isLoanPaidCompletely());

        verify(installmentRepository).save(installment1);
        verify(installmentRepository).save(installment2);
        verify(customerRepository).save(customer);
    }

    @Test
    @WithMockCustomUser(id = 100L)
    public void PayLoan_WhenAllInstallmentsPaidThenLoanShouldBeMarkedAsPaid() {
        Long loanId = 1L;
        Long customerID = 100L;
        double paymentAmount = 300.0;

        Loan loan = new Loan();
        loan.setCustomerId(customerID);
        loan.setId(loanId);
        loan.setIsPaid(false);

        Installment installment1 = new Installment();
        installment1.setId(1L);
        installment1.setLoanId(loanId);
        installment1.setAmount(BigDecimal.valueOf(100.00));
        installment1.setTotalAmount(BigDecimal.valueOf(105.00));
        installment1.setDueDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));
        installment1.setIsPaid(false);

        Installment installment2 = new Installment();
        installment2.setId(2L);
        installment2.setLoanId(loanId);
        installment2.setAmount(BigDecimal.valueOf(100.00));
        installment2.setTotalAmount(BigDecimal.valueOf(105.00));
        installment2.setDueDate(LocalDate.now().plusMonths(2).withDayOfMonth(1));
        installment2.setIsPaid(false);

        List<Installment> installments = new ArrayList<>();
        installments.add(installment1);
        installments.add(installment2);

        loan.setInstallments(installments);

        Customer customer = new Customer();
        customer.setId(customerID);
        customer.setUsername("customer1");

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(customerRepository.findById(customerID)).thenReturn(Optional.of(customer));
        when(installmentRepository.findAllByLoanIdAndIsPaidOrderByDueDateAsc(loanId, false)).thenReturn(new ArrayList<>());

        LoanPaymentInfo info = installmentService.payInstallment(customerID, loanId, paymentAmount);

        assertEquals(2, info.getPaidInstallments().size());
        assertTrue(info.isLoanPaidCompletely());

        verify(installmentRepository).save(installment1);
        verify(installmentRepository).save(installment2);
        verify(loanRepository).save(loan);
        verify(customerRepository).save(customer);
    }

    @Test
    @WithMockCustomUser(id = 100L)
    public void PayLoan_WhenInstallmentDueDateIsPassedThenPenaltyShouldBeApplied() {
        Long loanId = 1L;
        Long customerID = 100L;
        double paymentAmount = 300.0;

        Loan loan = new Loan();
        loan.setCustomerId(customerID);
        loan.setId(loanId);
        loan.setIsPaid(false);

        Installment installment1 = new Installment();
        installment1.setId(1L);
        installment1.setLoanId(loanId);
        installment1.setAmount(BigDecimal.valueOf(100.00));
        installment1.setTotalAmount(BigDecimal.valueOf(105.00));
        installment1.setDueDate(LocalDate.now().minusMonths(1));
        installment1.setIsPaid(false);

        Installment installment2 = new Installment();
        installment2.setId(2L);
        installment2.setLoanId(loanId);
        installment2.setAmount(BigDecimal.valueOf(100.00));
        installment2.setTotalAmount(BigDecimal.valueOf(105.00));
        installment2.setDueDate(LocalDate.now().plusMonths(2).withDayOfMonth(1));
        installment2.setIsPaid(false);

        Installment installment3 = new Installment();
        installment3.setId(3L);
        installment3.setLoanId(loanId);
        installment3.setAmount(BigDecimal.valueOf(100.00));
        installment3.setTotalAmount(BigDecimal.valueOf(105.00));
        installment3.setDueDate(LocalDate.now().plusMonths(4).withDayOfMonth(1));
        installment3.setIsPaid(false);

        List<Installment> installments = new ArrayList<>();
        installments.add(installment1);
        installments.add(installment2);

        loan.setInstallments(installments);

        Customer customer = new Customer();
        customer.setId(customerID);
        customer.setUsername("customer1");

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(customerRepository.findById(customerID)).thenReturn(Optional.of(customer));

        List<Installment> unpaidInstallments = new ArrayList<>();
        Installment unpaidInstallment = new Installment();
        unpaidInstallment.setId(4L);
        unpaidInstallments.add(unpaidInstallment);
        when(installmentRepository.findAllByLoanIdAndIsPaidOrderByDueDateAsc(loanId, false)).thenReturn(unpaidInstallments);

        LoanPaymentInfo info = installmentService.payInstallment(customerID, loanId, paymentAmount);

        assertEquals(2, info.getPaidInstallments().size());
        assertFalse(info.isLoanPaidCompletely());

        verify(installmentRepository).save(installment1);
        verify(installmentRepository).save(installment2);
        verify(customerRepository).save(customer);
    }

    @Test
    @WithMockCustomUser(id = 5L)
    public void PayLoan_WhenLoanNotFoundThenShouldThrowLoanNotFoundException() {
        Long loanId = 1L;
        Long customerID = 5L;
        double paymentAmount = 300.0;

        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> installmentService.payInstallment(customerID, loanId, paymentAmount));
    }

    @Test
    @WithMockCustomUser(id = 100L)
    public void PayLoan_WhenCustomerNotFoundThenShouldThrowCustomerNotFoundException() {
        Long loanId = 1L;
        Long customerID = 100L;
        double paymentAmount = 300.0;

        Loan loan = new Loan();
        loan.setCustomerId(customerID);
        loan.setId(loanId);
        loan.setIsPaid(false);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(customerRepository.findById(customerID)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> installmentService.payInstallment(customerID, loanId, paymentAmount));
    }

    @Test
    @WithMockCustomUser(id = 2L, role = "CUSTOMER")
    public void PayLoan_WhenLoanIsNotOwnedByAuthenticatedCustomerThenShouldReturnError() {
        Long loanId = 1L;
        Long customerID = 1L;
        double paymentAmount = 300.0;

        Loan loan = new Loan();
        loan.setCustomerId(customerID);
        loan.setId(loanId);
        loan.setIsPaid(false);

        Customer customer = new Customer();
        customer.setId(customerID);
        customer.setUsername("customer1");
        customer.setCreditLimit(BigDecimal.valueOf(5000.0));
        customer.setUsedCreditLimit(BigDecimal.valueOf(1000.0));

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(customerRepository.findById(customerID)).thenReturn(Optional.of(customer));

        assertThrows(AccessDeniedException.class, () -> installmentService.payInstallment(customerID, loanId, paymentAmount));
    }

    @Test
    @WithMockCustomUser()
    public void PayLoan_WhenLoanIsNotOwnedByAuthenticatedAdminCustomerButThenShouldProcessPayment() {
        Long loanId = 1L;
        Long customerID = 1L;
        double paymentAmount = 300.0;

        Loan loan = new Loan();
        loan.setCustomerId(customerID);
        loan.setId(loanId);
        loan.setIsPaid(false);

        Installment installment1 = new Installment();
        installment1.setId(1L);
        installment1.setLoanId(loanId);
        installment1.setAmount(BigDecimal.valueOf(100.00));
        installment1.setTotalAmount(BigDecimal.valueOf(105.00));
        installment1.setDueDate(LocalDate.now().minusMonths(1));
        installment1.setIsPaid(false);

        List<Installment> installments = new ArrayList<>();
        installments.add(installment1);

        loan.setInstallments(installments);

        Customer customer = new Customer();
        customer.setId(customerID);
        customer.setUsername("customer1");
        customer.setCreditLimit(BigDecimal.valueOf(5000.0));
        customer.setUsedCreditLimit(BigDecimal.valueOf(1000.0));

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(customerRepository.findById(customerID)).thenReturn(Optional.of(customer));

        List<Installment> unpaidInstallments = new ArrayList<>();
        Installment unpaidInstallment = new Installment();
        unpaidInstallment.setId(4L);
        unpaidInstallments.add(unpaidInstallment);
        when(installmentRepository.findAllByLoanIdAndIsPaidOrderByDueDateAsc(loanId, false)).thenReturn(unpaidInstallments);

        LoanPaymentInfo info = installmentService.payInstallment(customerID, loanId, paymentAmount);

        assertEquals(1, info.getPaidInstallments().size());
        assertFalse(info.isLoanPaidCompletely());

        verify(installmentRepository).save(installment1);
        verify(customerRepository).save(customer);
    }

    @Test
    @WithMockCustomUser(id = 5L)
    public void ListInstallments_WhenThereAreInstallmentsThenShouldReturnThemAsList() {
        Long loanID = 1L;
        Long customerID = 5L;
        Installment installment1 = new Installment();
        installment1.setLoanId(loanID);
        installment1.setAmount(BigDecimal.valueOf(1500.0));
        installment1.setTotalAmount(BigDecimal.valueOf(2000.0));
        installment1.setDueDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));

        Installment installment2 = new Installment();
        installment2.setLoanId(loanID);
        installment2.setAmount(BigDecimal.valueOf(1500.0));
        installment2.setTotalAmount(BigDecimal.valueOf(2000.0));
        installment2.setDueDate(LocalDate.now().plusMonths(2).withDayOfMonth(1));

        List<Installment> mockInstallments = List.of(installment1, installment2);

        Installment queryObject = new Installment();
        queryObject.setLoanId(loanID);

        when(installmentRepository.findAllByLoanId(loanID)).thenReturn(mockInstallments);

        List<Installment> result = installmentService.listInstallments(customerID, loanID);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(loanID, result.get(0).getLoanId());
        assertEquals(loanID, result.get(1).getLoanId());
        assertEquals(BigDecimal.valueOf(1500.0), result.get(0).getAmount());
        assertEquals(BigDecimal.valueOf(2000.0), result.get(0).getTotalAmount());
        assertEquals(BigDecimal.valueOf(1500.0), result.get(1).getAmount());
        assertEquals(BigDecimal.valueOf(2000.0), result.get(1).getTotalAmount());

        verify(installmentRepository, times(1)).findAllByLoanId(loanID);
    }

    @Test
    @WithMockCustomUser(id = 5L)
    public void ListInstallments_WhenThereIsNoInstallmentThenShouldReturnEmptyList() {
        Long loanID = 1L;
        Long customerID = 5L;

        Installment queryObject = new Installment();
        queryObject.setLoanId(loanID);

        List<Installment> mockInstallments = new ArrayList<>();

        when(installmentRepository.findAllByLoanId(loanID)).thenReturn(mockInstallments);

        List<Installment> result = installmentService.listInstallments(customerID, loanID);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(installmentRepository, times(1)).findAllByLoanId(loanID);
    }
}
