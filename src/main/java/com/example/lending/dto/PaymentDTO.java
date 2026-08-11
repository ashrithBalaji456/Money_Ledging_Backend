package com.example.lending.dto;

import com.example.lending.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {

    private Long id;
    private Long loanId;
    private Long borrowerId;
    private String borrowerName;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal expectedAmount;
    private BigDecimal paidAmount;
    private LocalDate paidDate;
    private PaymentStatus status;
    private boolean late;
    private long lateDays;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
