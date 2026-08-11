package com.example.lending.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentRecordRequest {

    @NotNull(message = "Paid amount is required")
    @DecimalMin(value = "0.01", message = "Paid amount must be greater than 0")
    private BigDecimal paidAmount;

    @NotNull(message = "Payment date is required")
    private LocalDate paidDate;

    private String notes;
}
