package com.safa.loanapi.loan.dao;

import com.safa.loanapi.installment.dao.Installment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;
    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;
    private Integer numberOfInstallments;
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    private Boolean isPaid;

    @OneToMany(mappedBy = "loanId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Installment> installments;

    public Loan(Long customerId, BigDecimal amount, BigDecimal totalAmount, Integer numberOfInstallments, Boolean isPaid) {
        this.customerId = customerId;
        this.amount = amount;
        this.totalAmount = totalAmount;
        this.numberOfInstallments = numberOfInstallments;
        this.isPaid = isPaid;
    }
}
