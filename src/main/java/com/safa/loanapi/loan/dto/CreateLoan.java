package com.safa.loanapi.loan.dto;

import com.safa.loanapi.validator.NumberOfInstallment;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateLoan {
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive value")
    private Double amount;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.1", message = "Rate must be at least 0.1")
    @DecimalMax(value = "0.5", message = "Rate must be at most 0.5")
    private Double rate;

    @NotNull(message = "Number of installment is required")
    @NumberOfInstallment(Values = {6, 9, 12, 24}, message = "Number of installment must be one of {Values}")
    private Integer numberOfInstallments;
}
