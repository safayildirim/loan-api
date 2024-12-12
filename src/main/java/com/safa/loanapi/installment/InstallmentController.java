package com.safa.loanapi.installment;

import com.safa.loanapi.installment.dao.Installment;
import com.safa.loanapi.installment.dto.PayInstallment;
import com.safa.loanapi.loan.dto.LoanPaymentInfo;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers/{customer_id}/loans")
public class InstallmentController {
    private final InstallmentService installmentService;

    public InstallmentController(InstallmentService installmentService) {
        this.installmentService = installmentService;
    }

    @GetMapping("/{loan_id}/installments")
    ResponseEntity<List<Installment>> listInstallments(@PathVariable Long customer_id, @PathVariable Long loan_id) {
        return ResponseEntity.ok(this.installmentService.listInstallments(customer_id, loan_id));
    }

    @PostMapping("/{loan_id}/payment")
    ResponseEntity<LoanPaymentInfo> payInstallment(@PathVariable Long customer_id, @PathVariable Long loan_id, @Valid @RequestBody PayInstallment req) {
        return ResponseEntity.ok(this.installmentService.payInstallment(customer_id, loan_id, req.getAmount()));
    }
}
