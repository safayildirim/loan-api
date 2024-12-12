package com.safa.loanapi.installment;

import com.safa.loanapi.customer.CustomerRepository;
import com.safa.loanapi.customer.dao.Customer;
import com.safa.loanapi.exception.CustomerNotFoundException;
import com.safa.loanapi.exception.LoanNotFoundException;
import com.safa.loanapi.installment.dao.Installment;
import com.safa.loanapi.loan.LoanRepository;
import com.safa.loanapi.loan.dao.Loan;
import com.safa.loanapi.loan.dto.LoanPaymentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static com.safa.loanapi.common.Common.roundTwoDecimal;

@Service
public class InstallmentService {
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private InstallmentRepository installmentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Retrieves the list of installments associated with a given loan ID.
     *
     * <p>This method enforces security checks using the {@code @PreAuthorize} annotation:
     * the authenticated user's ID (from {@code authentication.principal.id}) must match the provided
     * {@code customerId}, or the user must have the {@code ADMIN} role to access the resource.</p>
     *
     * @param customerId the ID of the customer requesting the installments. This must match the
     *                   authenticated user's ID unless the user has the {@code ADMIN} role.
     * @param loanID     the ID of the loan for which the installments are requested.
     * @return a list of installments associated with the specified loan ID.
     * @throws org.springframework.security.access.AccessDeniedException if the security condition
     *                                                                   specified in the {@code @PreAuthorize} annotation is not met.
     */
    @PreAuthorize("#customerId == authentication.principal.id or hasRole('ADMIN')")
    public List<Installment> listInstallments(Long customerId, Long loanID) {
        return installmentRepository.findAllByLoanId(loanID);
    }


    /**
     * Processes the payment of installments for a specified loan.
     * <p>
     * This method allows a user to pay off one or more installments of a loan, considering discounts for early payments
     * and penalties for late payments. It updates the installment status, recalculates the customer's used credit limit,
     * and checks if the loan is fully paid. Only the loan owner or users with the {@code ADMIN} role can make payments.
     * </p>
     *
     * @param customerId the user id of the user making the payment
     * @param loanId     the ID of the loan for which the payment is being made
     * @param amount     the amount to be applied toward the payment of installments
     * @return a {@link LoanPaymentInfo} object containing details about the paid installments and payment status
     * @throws LoanNotFoundException     if the loan with the specified ID does not exist
     * @throws CustomerNotFoundException if the customer associated with the loan does not exist
     * @throws AccessDeniedException     if the user is not authorized to make payments for the specified loan
     */
    @PreAuthorize("#customerId == authentication.principal.id or hasRole('ADMIN')")
    public LoanPaymentInfo payInstallment(Long customerId, Long loanId, double amount) {
        // Retrieve the loan by ID, throw an exception if it doesn't exist
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new LoanNotFoundException(loanId));

        // Retrieve the customer by ID, throw an exception if it doesn't exist
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new CustomerNotFoundException(loan.getCustomerId()));

        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();

        // Filter the loan's installments to include only unpaid ones that are not older than 3 months,
        // and sort them by due date
        List<Installment> payableInstallment = loan.getInstallments().stream().
                filter(installment -> !installment.getIsPaid() && !installment.getDueDate().minusMonths(3).isAfter(LocalDate.now())).
                sorted(Comparator.comparing(Installment::getDueDate)).toList();

        // Initialize the object to store payment information
        LoanPaymentInfo loanPaymentInfo = new LoanPaymentInfo();

        // Variable to track the credit limit adjustment
        double addedCreditLimit = 0;

        // Process each payable installment
        for (Installment installment : payableInstallment) {
            BigDecimal amountToPay = installment.getTotalAmount();

            // Apply a discount if the installment is not yet due
            if (installment.getDueDate().isAfter(now.toLocalDate())) {
                long numOfDays = ChronoUnit.DAYS.between(now.toLocalDate(), installment.getDueDate());
                BigDecimal discount = BigDecimal.valueOf(installment.getTotalAmount().doubleValue() * 0.001 * numOfDays).setScale(2, RoundingMode.HALF_EVEN);
                amountToPay = amountToPay.subtract(discount);
            }

            // Apply a penalty if the installment is overdue
            if (installment.getDueDate().isBefore(now.toLocalDate())) {
                long numOfDays = ChronoUnit.DAYS.between(installment.getDueDate(), now.toLocalDate());
                BigDecimal penalty = BigDecimal.valueOf(installment.getTotalAmount().doubleValue() * 0.001 * numOfDays).setScale(2, RoundingMode.HALF_EVEN);
                ;
                amountToPay = amountToPay.add(penalty);
            }

            // If the remaining payment amount is less than the amount due for this installment, stop processing
            if (amount < amountToPay.doubleValue()) {
                break;
            }

            // Mark the installment as paid, record the paid amount and payment date
            installment.setIsPaid(true);
            installment.setPaidAmount(amountToPay);
            installment.setPaymentDate(now);
            this.installmentRepository.save(installment);

            // Update the credit limit adjustment for the customer
            addedCreditLimit += installment.getAmount().doubleValue();

            // Deduct the paid amount from the total payment
            amount -= amountToPay.doubleValue();

            // Add the installment to the payment information
            loanPaymentInfo.getPaidInstallments().add(installment);
            loanPaymentInfo.setTotalAmountSpent(roundTwoDecimal(loanPaymentInfo.getTotalAmountSpent() + amountToPay.doubleValue()));
        }

        // Check if there are no unpaid installments remaining
        List<Installment> unpaidInstallments = this.installmentRepository.findAllByLoanIdAndIsPaidOrderByDueDateAsc(loanId, false);
        if (unpaidInstallments.isEmpty()) {
            // If all installments are paid, mark the loan as fully paid
            loan.setIsPaid(true);
            this.loanRepository.save(loan);
            loanPaymentInfo.setLoanPaidCompletely(true);
        }

        // Update the customer's used credit limit by subtracting the paid amount
        customer.setUsedCreditLimit(customer.getUsedCreditLimit().subtract(BigDecimal.valueOf(addedCreditLimit)));
        this.customerRepository.save(customer);

        // Return the payment summary
        return loanPaymentInfo;
    }
}
