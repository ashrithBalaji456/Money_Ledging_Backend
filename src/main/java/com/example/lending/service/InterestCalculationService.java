package com.example.lending.service;

import com.example.lending.entity.InterestRateType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class InterestCalculationService {

    public static class CalculationResult {
        public final BigDecimal interestAmount;
        public final BigDecimal totalPayable;
        public final BigDecimal monthlyInstallment;

        public CalculationResult(BigDecimal interestAmount, BigDecimal totalPayable, BigDecimal monthlyInstallment) {
            this.interestAmount = interestAmount;
            this.totalPayable = totalPayable;
            this.monthlyInstallment = monthlyInstallment;
        }
    }

    /**
     * Calculates interest, total payable amount, and monthly installment.
     */
    public CalculationResult calculateSimpleInterest(
            BigDecimal principal,
            BigDecimal rate,
            InterestRateType rateType,
            int durationInMonths
    ) {
        if (principal == null || rate == null || rateType == null || durationInMonths <= 0) {
            throw new IllegalArgumentException("Invalid arguments for interest calculation");
        }

        BigDecimal duration = BigDecimal.valueOf(durationInMonths);
        BigDecimal interestAmount;

        if (rateType == InterestRateType.ANNUAL) {
            // Interest = Principal * (Rate / 100) * (Duration / 12)
            BigDecimal rateFactor = rate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
            BigDecimal timeFactor = duration.divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP);
            interestAmount = principal.multiply(rateFactor).multiply(timeFactor).setScale(2, RoundingMode.HALF_UP);
        } else {
            // Interest = Principal * (Rate / 100) * Duration
            BigDecimal rateFactor = rate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
            interestAmount = principal.multiply(rateFactor).multiply(duration).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalPayable = principal.add(interestAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyInstallment = totalPayable.divide(duration, 2, RoundingMode.HALF_UP);

        return new CalculationResult(interestAmount, totalPayable, monthlyInstallment);
    }
}
