package com.example.lending.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPreviewResponse {
    private BigDecimal principalAmount;
    private BigDecimal totalInterest;
    private BigDecimal totalPayableAmount;
    private BigDecimal monthlyInstallment;
}
