package com.safa.loanapi.installment;

import com.safa.loanapi.installment.dao.Installment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {
    List<Installment> findAllByLoanIdAndIsPaidOrderByDueDateAsc(Long loanId, boolean isPaid);
    List<Installment> findAllByLoanId(Long loanId);
}
