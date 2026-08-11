package com.example.lending.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowerDTO {

    private Long id;

    @NotBlank(message = "Name cannot be empty")
    private String fullName;

    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(regexp = "^\\+?[0-9\\s\\-]{10,15}$", message = "Phone number must be valid")
    private String phoneNumber;

    private String address;

    @Pattern(regexp = "^$|^[0-9]{4}$", message = "Aadhaar number must contain only the last 4 digits")
    private String aadharLastFourDigits;

    private String notes;

    private boolean active;

    private Integer activeLoans;
    private java.math.BigDecimal outstandingAmount;
    private java.time.LocalDate nextPaymentDate;
    private java.time.LocalDate loanStartDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
