package com.safa.loanapi.installment.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Installment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long loanId;
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;
    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;
    @Column(precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.valueOf(0.0);
    private LocalDate dueDate;
    private LocalDateTime paymentDate;
    private Boolean isPaid = false;

    public Installment(Long loanId, BigDecimal amount, BigDecimal totalAmount, LocalDate dueDate) {
        this.loanId = loanId;
        this.amount = amount;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
    }
}
