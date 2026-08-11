package com.example.lending.service;

import com.example.lending.dto.PaymentRecordRequest;
import com.example.lending.entity.*;
import com.example.lending.repository.LoanRepository;
import com.example.lending.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LendingServiceTests {

    @InjectMocks
    private InterestCalculationService interestCalculationService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInterestCalculation() {
        // Principal = ₹100,000, Rate = 12% annual, Duration = 12 months
        // Expected interest = ₹12,000
        BigDecimal principal = BigDecimal.valueOf(100000.00);
        BigDecimal rate = BigDecimal.valueOf(12.00);
        InterestRateType rateType = InterestRateType.ANNUAL;
        int duration = 12;

        InterestCalculationService.CalculationResult result = interestCalculationService.calculateSimpleInterest(
                principal, rate, rateType, duration
        );

        assertEquals(0, result.interestAmount.compareTo(BigDecimal.valueOf(12000.00)), "Interest should be ₹12,000.00");
        assertEquals(0, result.totalPayable.compareTo(BigDecimal.valueOf(112000.00)), "Total payable should be ₹112,000.00");
        assertEquals(0, result.monthlyInstallment.compareTo(BigDecimal.valueOf(9333.33)), "Monthly installment should be ₹9,333.33");
    }

    @Test
    void testMonthlyPaymentCalculation() {
        // Total = ₹120,000, Months = 12
        // Expected monthly payment = ₹10,000
        BigDecimal principal = BigDecimal.valueOf(120000.00);
        // Under simple interest, if we want total payable = 120,000, we can use 0% interest for direct testing of payment logic
        BigDecimal rate = BigDecimal.ZERO;
        InterestRateType rateType = InterestRateType.ANNUAL;
        int duration = 12;

        InterestCalculationService.CalculationResult result = interestCalculationService.calculateSimpleInterest(
                principal, rate, rateType, duration
        );

        assertEquals(0, result.totalPayable.compareTo(BigDecimal.valueOf(120000.00)), "Total payable should be ₹120,000.00");
        assertEquals(0, result.monthlyInstallment.compareTo(BigDecimal.valueOf(10000.00)), "Monthly installment should be ₹10,000.00");
    }

    @Test
    void testDueSoonAndOverdueStatusDerivation() {
        LocalDate today = LocalDate.now();

        // 1. Due today -> DUE_SOON
        Payment pToday = Payment.builder().dueDate(today).expectedAmount(BigDecimal.TEN).paidAmount(BigDecimal.ZERO).build();
        assertEquals(PaymentStatus.DUE_SOON, pToday.getDerivedStatus(today));

        // 2. Due tomorrow -> DUE_SOON
        Payment pTomorrow = Payment.builder().dueDate(today.plusDays(1)).expectedAmount(BigDecimal.TEN).paidAmount(BigDecimal.ZERO).build();
        assertEquals(PaymentStatus.DUE_SOON, pTomorrow.getDerivedStatus(today));

        // 3. Due in 2 days -> DUE_SOON
        Payment p2Days = Payment.builder().dueDate(today.plusDays(2)).expectedAmount(BigDecimal.TEN).paidAmount(BigDecimal.ZERO).build();
        assertEquals(PaymentStatus.DUE_SOON, p2Days.getDerivedStatus(today));

        // 4. Due in 3 days -> DUE_SOON
        Payment p3Days = Payment.builder().dueDate(today.plusDays(3)).expectedAmount(BigDecimal.TEN).paidAmount(BigDecimal.ZERO).build();
        assertEquals(PaymentStatus.DUE_SOON, p3Days.getDerivedStatus(today));

        // 5. Due in 4 days -> UPCOMING (Not due soon)
        Payment p4Days = Payment.builder().dueDate(today.plusDays(4)).expectedAmount(BigDecimal.TEN).paidAmount(BigDecimal.ZERO).build();
        assertEquals(PaymentStatus.UPCOMING, p4Days.getDerivedStatus(today));

        // 6. Due yesterday -> OVERDUE
        Payment pYesterday = Payment.builder().dueDate(today.minusDays(1)).expectedAmount(BigDecimal.TEN).paidAmount(BigDecimal.ZERO).build();
        assertEquals(PaymentStatus.OVERDUE, pYesterday.getDerivedStatus(today));

        // 7. Due 5 days ago -> OVERDUE
        Payment p5DaysAgo = Payment.builder().dueDate(today.minusDays(5)).expectedAmount(BigDecimal.TEN).paidAmount(BigDecimal.ZERO).build();
        assertEquals(PaymentStatus.OVERDUE, p5DaysAgo.getDerivedStatus(today));
    }

    @Test
    void testPaidStatusDerivation() {
        LocalDate today = LocalDate.now();

        // Expected = 10, Paid = 10 -> PAID
        Payment pPaid = Payment.builder().dueDate(today.plusDays(5)).expectedAmount(BigDecimal.valueOf(10.00)).paidAmount(BigDecimal.valueOf(10.00)).build();
        assertEquals(PaymentStatus.PAID, pPaid.getDerivedStatus(today));
    }

    @Test
    void testPartialPaymentStatusDerivation() {
        LocalDate today = LocalDate.now();

        // Expected = 10, Paid = 6 -> PARTIALLY_PAID
        Payment pPartial = Payment.builder().dueDate(today.plusDays(5)).expectedAmount(BigDecimal.valueOf(10.00)).paidAmount(BigDecimal.valueOf(6.00)).build();
        assertEquals(PaymentStatus.PARTIALLY_PAID, pPartial.getDerivedStatus(today));
    }

    @Test
    void testRecordPaymentOperation() {
        LocalDate today = LocalDate.now();
        Borrower borrower = Borrower.builder().id(1L).fullName("Ramesh").active(true).build();
        Loan loan = Loan.builder()
                .id(1L)
                .borrower(borrower)
                .principalAmount(BigDecimal.valueOf(10000.00))
                .totalPayableAmount(BigDecimal.valueOf(12000.00))
                .remainingPrincipal(BigDecimal.valueOf(10000.00))
                .remainingAmount(BigDecimal.valueOf(12000.00))
                .status(LoanStatus.ACTIVE)
                .build();

        Payment payment = Payment.builder()
                .id(10L)
                .loan(loan)
                .installmentNumber(1)
                .dueDate(today)
                .expectedAmount(BigDecimal.valueOf(1000.00))
                .paidAmount(BigDecimal.ZERO)
                .status(PaymentStatus.UPCOMING)
                .build();

        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        when(paymentRepository.findByLoanIdOrderByInstallmentNumberAsc(1L)).thenReturn(Collections.singletonList(payment));

        PaymentRecordRequest recordRequest = new PaymentRecordRequest();
        recordRequest.setPaidAmount(BigDecimal.valueOf(1000.00));
        recordRequest.setPaidDate(today);
        recordRequest.setNotes("First payment");

        com.example.lending.dto.PaymentDTO response = paymentService.recordPayment(10L, recordRequest);

        // Verify status becomes PAID
        assertEquals(PaymentStatus.PAID, response.getStatus());
        assertEquals(0, response.getPaidAmount().compareTo(BigDecimal.valueOf(1000.00)));

        // Verify Loan remaining amounts decrease
        // Proportional Principal paid = 1000 * (10000 / 12000) = 833.33
        assertEquals(0, loan.getRemainingAmount().compareTo(BigDecimal.valueOf(11000.00)));
        assertEquals(0, loan.getRemainingPrincipal().compareTo(BigDecimal.valueOf(9166.67)));
        
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(loanRepository, times(1)).save(any(Loan.class));
    }
}
