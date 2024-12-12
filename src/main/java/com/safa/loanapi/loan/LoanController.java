package com.safa.loanapi.loan;

import com.safa.loanapi.loan.dao.Loan;
import com.safa.loanapi.loan.dto.CreateLoan;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class LoanController {
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/{customer_id}/loans")
    ResponseEntity<Loan> createLoan(@Valid @RequestBody CreateLoan req, @PathVariable Long customer_id) {
        return ResponseEntity.ok(this.loanService.createLoan(customer_id, req.getAmount(), req.getRate(), req.getNumberOfInstallments()));
    }

    @GetMapping("/{customer_id}/loans")
    ResponseEntity<List<Loan>> listLoans(@PathVariable Long customer_id) {
        return ResponseEntity.ok(this.loanService.listLoans(customer_id));
    }

}
