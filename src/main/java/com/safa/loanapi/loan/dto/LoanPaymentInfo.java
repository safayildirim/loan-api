package com.safa.loanapi.loan.dto;

import com.safa.loanapi.installment.dao.Installment;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LoanPaymentInfo {
    private List<Installment> paidInstallments = new ArrayList<>();
    double totalAmountSpent;
    boolean loanPaidCompletely;
}
