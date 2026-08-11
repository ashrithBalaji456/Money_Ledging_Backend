package com.example.lending.dto;

import com.example.lending.entity.InterestRateType;
import com.example.lending.entity.InterestType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanCreationRequest {

    @NotNull(message = "Borrower ID is required")
    private Long borrowerId;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "0.01", message = "Principal amount must be greater than 0")
    private BigDecimal principalAmount;

    // Optional for FIXED_INTEREST, required for SIMPLE_INTEREST
    private BigDecimal interestRate;

    @NotNull(message = "Interest calculation type is required")
    private InterestType interestType = InterestType.SIMPLE_INTEREST;

    // Optional for FIXED_INTEREST, required for SIMPLE_INTEREST
    private InterestRateType interestRateType = InterestRateType.ANNUAL;

    @NotNull(message = "Loan start date is required")
    private LocalDate loanStartDate;

    @NotNull(message = "Duration in months is required")
    @Min(value = 1, message = "Duration must be greater than 0")
    private Integer durationInMonths;

    private BigDecimal customInterestAmount;

    private String notes;
}
