package com.safa.loanapi.loan;

import com.safa.loanapi.common.Common;
import com.safa.loanapi.customer.CustomerRepository;
import com.safa.loanapi.customer.dao.Customer;
import com.safa.loanapi.exception.CustomerNotFoundException;
import com.safa.loanapi.exception.NotEnoughLimitException;
import com.safa.loanapi.installment.InstallmentRepository;
import com.safa.loanapi.installment.dao.Installment;
import com.safa.loanapi.loan.dao.Loan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private InstallmentRepository installmentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Creates a loan for the specified customer with the given details.
     * <p>
     * This method ensures that:
     * <ul>
     *   <li>The customer exists in the system.</li>
     *   <li>The customer has sufficient credit limit to take out the loan.</li>
     * </ul>
     * The loan details, including installment amounts and schedule, are calculated and stored.
     * Additionally, the customer's used credit limit is updated to reflect the new loan.
     * </p>
     * <p>
     * Authorization rules:
     * <ul>
     *   <li>A user with the {@code CUSTOMER} role can create a loan only for their own account.</li>
     *   <li>A user with the {@code ADMIN} role can create loans for any customer.</li>
     * </ul>
     * </p>
     *
     * @param customerID        the ID of the customer taking the loan
     * @param amount            the amount of the loan
     * @param rate              the interest rate for the loan as a decimal (e.g., 0.1 for 10%)
     * @param numOfInstallments the number of installments in which the loan will be repaid
     * @return the created {@link Loan} object, including calculated total amount and installments
     * @throws CustomerNotFoundException if the customer with the specified ID does not exist
     * @throws NotEnoughLimitException   if the customer does not have enough available credit limit to take the loan
     */
    @PreAuthorize("#customerID == authentication.principal.id or hasRole('ADMIN')")
    public Loan createLoan(long customerID, double amount, double rate, int numOfInstallments) {
        // Fetch the customer by ID; throw an exception if the customer does not exist
        Customer customer = customerRepository.findById(customerID).orElseThrow(() -> new CustomerNotFoundException(customerID));

        // Retrieve the customer's credit limit and used credit limit
        BigDecimal customerLimit = customer.getCreditLimit();
        BigDecimal usedCustomerCreditLimit = customer.getUsedCreditLimit();

        // Check if the customer has enough available credit to take out the loan
        if (customerLimit.subtract(usedCustomerCreditLimit).doubleValue() < amount) {
            throw new NotEnoughLimitException(customerID);
        }

        // Round the loan amount to two decimal places for accuracy
        amount = Common.roundTwoDecimal(amount);

        // Calculate the total loan amount with interest
        double totalLoanAmount = amount * (1 + rate);

        // Divide the principal amount and total loan amount into equal installments
        List<Double> installmentAmounts = divideEvenly(amount, numOfInstallments);
        List<Double> totalInstallmentAmounts = divideEvenly(totalLoanAmount, numOfInstallments);

        // Create a new loan object with the specified details
        Loan loan = new Loan(customerID, BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_EVEN),
                BigDecimal.valueOf(totalLoanAmount).setScale(2, RoundingMode.HALF_EVEN), numOfInstallments, false);

        // Save the loan to the database
        loan = loanRepository.save(loan);

        // Initialize the loan's installment list
        loan.setInstallments(new ArrayList<>());

        // Create and save each installment
        for (int i = 0; i < totalInstallmentAmounts.size(); i++) {
            Double installmentAmount = installmentAmounts.get(i);
            Double totalInstallmentAmount = totalInstallmentAmounts.get(i);

            // Create an installment with the specified due date, amount, and total amount
            Installment installment = new Installment(loan.getId(), BigDecimal.valueOf(installmentAmount), BigDecimal.valueOf(totalInstallmentAmount),
                    LocalDate.now().plusMonths(i + 1).withDayOfMonth(1));

            // Save the installment to the database and add it to the loan
            installmentRepository.save(installment);
            loan.getInstallments().add(installment);
        }

        // Update the customer's used credit limit by adding the loan amount
        customer.setUsedCreditLimit(customer.getUsedCreditLimit().add(BigDecimal.valueOf(amount)));
        customerRepository.save(customer);

        // Return the created loan object
        return loan;
    }

    /**
     * Retrieves a list of loans associated with the specified customer ID.
     * <p>
     * This method enforces authorization rules:
     * <ul>
     *   <li>A user with the {@code CUSTOMER} role can only retrieve loans associated with their own customer ID.</li>
     *   <li>A user with the {@code ADMIN} role can retrieve loans for any customer.</li>
     * </ul>
     * </p>
     *
     * @param customerID the ID of the customer whose loans are to be retrieved
     * @return a list of {@link Loan} objects belonging to the specified customer
     * @throws AccessDeniedException if the authenticated user does not have permission
     *                               to access the specified customer's loans
     */
    @PreAuthorize("#customerID == authentication.principal.id or hasRole('ADMIN')")
    public List<Loan> listLoans(Long customerID) {
        Loan queryObject = new Loan();
        queryObject.setCustomerId(customerID);
        return loanRepository.findAll(Example.of(queryObject));
    }

    /**
     * Divides a given amount into nearly equal parts based on the specified divider, ensuring the total sum matches the original amount.
     * This method handles rounding issues by distributing the remainder as evenly as possible.
     *
     * @param amount  the total amount to be divided, represented as a double.
     * @param divider the number of parts to divide the amount into.
     * @return a list of doubles representing the divided parts, where some parts may be slightly larger to account for rounding.
     *
     * <p>For example, if the amount is 100.0 and the divider is 3, the method will return a list like [33.33, 33.33, 33.34].</p>
     * <br>
     * <p>Implementation Details:</p>
     * <ul>
     *   <li>The amount is scaled to an integer (multiplied by 100) to ensure precision during division.</li>
     *   <li>Each part is calculated either as the lower or higher value (in cents) to minimize rounding discrepancies.</li>
     *   <li>The method ensures the sum of the returned parts equals the original amount.</li>
     * </ul>
     * @throws ArithmeticException if the divider is less than or equal to zero.
     */
    public static List<Double> divideEvenly(double amount, double divider) {
        // Throw an exception if the divider is zero to prevent division by zero
        if (divider == 0) throw new ArithmeticException();

        // List to store the resulting parts
        List<Double> nums = new ArrayList<>();

        // Convert the amount to an integer value representing the smallest unit of currency (multiplied by 100)
        int a = (int) (100 * amount);

        // Calculate the base low value for each part
        int low_value = (int) (a / divider);

        // Calculate the base high value, which is one cent higher than the low value
        int high_value = low_value + 1;

        // Calculate how many parts need to have the high value
        int num_highs = (int) (a % divider);

        // Calculate how many parts need to have the low value
        int num_lows = (int) (divider - num_highs);

        // Add the low value parts to the list
        for (int i = 0; i < num_lows; i++) {
            nums.add((double) low_value / 100);
        }

        // Add the high value parts to the list
        for (int i = 0; i < num_highs; i++) {
            nums.add((double) high_value / 100);
        }

        // Return the list of divided parts
        return nums;
    }
}
