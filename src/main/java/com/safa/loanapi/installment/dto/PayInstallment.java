package com.safa.loanapi.installment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PayInstallment {
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive value")
    private Double amount;
}
