package com.example.lending.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;

    @Column(name = "principal_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_type", nullable = false)
    private InterestType interestType = InterestType.SIMPLE_INTEREST;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_rate_type", nullable = false)
    private InterestRateType interestRateType = InterestRateType.ANNUAL;

    @Column(name = "loan_start_date", nullable = false)
    private LocalDate loanStartDate;

    @Column(name = "duration_in_months", nullable = false)
    private Integer durationInMonths;

    @Column(name = "total_interest", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInterest;

    @Column(name = "total_payable_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPayableAmount;

    @Column(name = "monthly_installment", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyInstallment;

    @Column(name = "remaining_principal", nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingPrincipal;

    @Column(name = "remaining_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingAmount;

    @Column(name = "next_payment_date")
    private LocalDate nextPaymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = LoanStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
