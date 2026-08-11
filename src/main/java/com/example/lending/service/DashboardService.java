package com.example.lending.service;

import com.example.lending.dto.DashboardSummaryDTO;
import com.example.lending.entity.Loan;
import com.example.lending.entity.LoanStatus;
import com.example.lending.entity.Payment;
import com.example.lending.entity.PaymentStatus;
import com.example.lending.repository.LoanRepository;
import com.example.lending.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LoanRepository loanRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary() {
        List<Loan> allLoans = loanRepository.findAllWithActiveBorrowers();
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);

        BigDecimal totalLent = BigDecimal.ZERO;
        BigDecimal totalCollected = BigDecimal.ZERO;
        BigDecimal outstanding = BigDecimal.ZERO;
        long activeLoansCount = 0;

        for (Loan loan : allLoans) {
            if (loan.getStatus() != LoanStatus.CANCELLED) {
                totalLent = totalLent.add(loan.getPrincipalAmount());
                // outstanding balance remaining to be paid
                outstanding = outstanding.add(loan.getRemainingAmount());
                
                // Collected on this loan = total payable - remaining amount
                BigDecimal collected = loan.getTotalPayableAmount().subtract(loan.getRemainingAmount());
                totalCollected = totalCollected.add(collected);

                if (loan.getStatus() == LoanStatus.ACTIVE || loan.getStatus() == LoanStatus.OVERDUE) {
                    activeLoansCount++;
                }
            }
        }

        // Calculate due soon count (unpaid and due in next 3 days)
        List<Payment> dueSoonPayments = paymentRepository.findPaymentsDueSoon(today, threeDaysLater);
        long dueSoonCount = dueSoonPayments.size();

        // Calculate overdue count (unpaid and due date passed)
        List<Payment> overduePayments = paymentRepository.findOverduePayments(today);
        long overdueCount = overduePayments.size();

        return DashboardSummaryDTO.builder()
                .totalLent(totalLent)
                .totalCollected(totalCollected)
                .outstanding(outstanding)
                .dueSoonCount(dueSoonCount)
                .overdueCount(overdueCount)
                .activeLoansCount(activeLoansCount)
                .build();
    }
}
