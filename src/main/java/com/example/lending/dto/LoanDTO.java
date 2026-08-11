package com.example.lending.dto;

import com.example.lending.entity.InterestRateType;
import com.example.lending.entity.InterestType;
import com.example.lending.entity.LoanStatus;
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
public class LoanDTO {

    private Long id;
    private Long borrowerId;
    private String borrowerName;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private InterestType interestType;
    private InterestRateType interestRateType;
    private LocalDate loanStartDate;
    private Integer durationInMonths;
    private BigDecimal totalInterest;
    private BigDecimal totalPayableAmount;
    private BigDecimal monthlyInstallment;
    private BigDecimal remainingPrincipal;
    private BigDecimal remainingAmount;
    private LocalDate nextPaymentDate;
    private LoanStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
