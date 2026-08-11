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
public class DashboardSummaryDTO {
    private BigDecimal totalLent;
    private BigDecimal totalCollected;
    private BigDecimal outstanding;
    private long dueSoonCount;
    private long overdueCount;
    private long activeLoansCount;
}
