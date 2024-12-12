package com.safa.loanapi.loan;

import com.safa.loanapi.loan.dao.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
}
