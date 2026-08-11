package com.example.lending.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "expected_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal expectedAmount;

    @Column(name = "paid_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.UPCOMING;

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
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        if (status == null) {
            status = PaymentStatus.UPCOMING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Derives the status of this payment at runtime based on the given date (typically today).
     */
    public PaymentStatus getDerivedStatus(LocalDate date) {
        if (paidAmount != null && paidAmount.compareTo(expectedAmount) >= 0) {
            return PaymentStatus.PAID;
        }
        if (paidAmount != null && paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            return PaymentStatus.PARTIALLY_PAID;
        }
        if (date.isAfter(dueDate)) {
            return PaymentStatus.OVERDUE;
        }
        // Due today or in the next 3 days
        if (!dueDate.isAfter(date.plusDays(3))) {
            return PaymentStatus.DUE_SOON;
        }
        return PaymentStatus.UPCOMING;
    }
}
