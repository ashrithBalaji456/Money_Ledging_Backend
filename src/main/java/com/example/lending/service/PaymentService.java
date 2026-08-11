package com.example.lending.service;

import com.example.lending.dto.PaymentDTO;
import com.example.lending.dto.PaymentRecordRequest;
import com.example.lending.entity.Loan;
import com.example.lending.entity.LoanStatus;
import com.example.lending.entity.Payment;
import com.example.lending.entity.PaymentStatus;
import com.example.lending.exception.InvalidPaymentException;
import com.example.lending.exception.ResourceNotFoundException;
import com.example.lending.repository.LoanRepository;
import com.example.lending.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import com.example.lending.entity.User;
import com.example.lending.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByLoanId(Long loanId) {
        User currentUser = getCurrentUser();
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));
        if (loan.getBorrower().getUser() == null || !loan.getBorrower().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied to view payments for loan: " + loanId);
        }
        return paymentRepository.findByLoanIdOrderByInstallmentNumberAsc(loanId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long id) {
        User currentUser = getCurrentUser();
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment installment not found with id: " + id));
        if (payment.getLoan().getBorrower().getUser() == null || !payment.getLoan().getBorrower().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied to view payment installment: " + id);
        }
        return mapToDTO(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getDueSoonPayments() {
        User currentUser = getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);
        return paymentRepository.findPaymentsDueSoon(today, threeDaysLater, currentUser.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getOverduePayments() {
        User currentUser = getCurrentUser();
        LocalDate today = LocalDate.now();
        return paymentRepository.findOverduePayments(today, currentUser.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getRecentPayments() {
        User currentUser = getCurrentUser();
        return paymentRepository.findRecentPayments(currentUser.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentDTO recordPayment(Long id, PaymentRecordRequest request) {
        User currentUser = getCurrentUser();
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment installment not found with id: " + id));

        if (payment.getLoan().getBorrower().getUser() == null || !payment.getLoan().getBorrower().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied to record payment for installment: " + id);
        }

        BigDecimal remainingExpected = payment.getExpectedAmount().subtract(payment.getPaidAmount());
        if (remainingExpected.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException("This payment installment is already fully paid");
        }

        // Standard payment should pay off the remaining expected amount
        BigDecimal amountToPay = request.getPaidAmount();
        if (amountToPay.compareTo(remainingExpected) > 0) {
            throw new InvalidPaymentException("Payment amount exceeds outstanding expected amount of " + remainingExpected);
        }

        return processPaymentDetails(payment, amountToPay, request.getPaidDate(), request.getNotes());
    }

    @Transactional
    public PaymentDTO recordPartialPayment(Long id, PaymentRecordRequest request) {
        User currentUser = getCurrentUser();
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment installment not found with id: " + id));

        if (payment.getLoan().getBorrower().getUser() == null || !payment.getLoan().getBorrower().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied to record partial payment for installment: " + id);
        }

        BigDecimal remainingExpected = payment.getExpectedAmount().subtract(payment.getPaidAmount());
        if (remainingExpected.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException("This payment installment is already fully paid");
        }

        BigDecimal amountToPay = request.getPaidAmount();
        if (amountToPay.compareTo(remainingExpected) > 0) {
            throw new InvalidPaymentException("Partial payment amount " + amountToPay + " cannot exceed remaining expected amount of " + remainingExpected);
        }

        return processPaymentDetails(payment, amountToPay, request.getPaidDate(), request.getNotes());
    }

    private PaymentDTO processPaymentDetails(Payment payment, BigDecimal paidAmount, LocalDate paidDate, String notes) {
        Loan loan = payment.getLoan();

        // Update Payment
        payment.setPaidAmount(payment.getPaidAmount().add(paidAmount));
        payment.setPaidDate(paidDate);
        if (notes != null && !notes.trim().isEmpty()) {
            payment.setNotes(notes);
        }

        // Determine derived status
        LocalDate today = LocalDate.now();
        PaymentStatus derivedStatus = payment.getDerivedStatus(today);
        payment.setStatus(derivedStatus);
        
        paymentRepository.save(payment);

        // Update Loan Balances
        // Calculate principal paid proportionally: Principal Portion = Paid Amount * (Principal / Total Payable)
        BigDecimal principalPaid = paidAmount.multiply(loan.getPrincipalAmount())
                .divide(loan.getTotalPayableAmount(), 2, RoundingMode.HALF_UP);

        loan.setRemainingPrincipal(loan.getRemainingPrincipal().subtract(principalPaid).max(BigDecimal.ZERO));
        loan.setRemainingAmount(loan.getRemainingAmount().subtract(paidAmount).max(BigDecimal.ZERO));

        // If loan is fully settled, mark as completed
        if (loan.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.COMPLETED);
            loan.setNextPaymentDate(null);
        } else {
            // Find the next payment date (first unpaid payment)
            List<Payment> allPayments = paymentRepository.findByLoanIdOrderByInstallmentNumberAsc(loan.getId());
            LocalDate nextDate = null;
            for (Payment p : allPayments) {
                if (p.getPaidAmount().compareTo(p.getExpectedAmount()) < 0) {
                    nextDate = p.getDueDate();
                    break;
                }
            }
            loan.setNextPaymentDate(nextDate);
            
            // Check if loan should be flagged as OVERDUE
            // A loan is OVERDUE if its next payment date is in the past
            if (nextDate != null && nextDate.isBefore(today)) {
                loan.setStatus(LoanStatus.OVERDUE);
            } else {
                loan.setStatus(LoanStatus.ACTIVE);
            }
        }

        loanRepository.save(loan);

        return mapToDTO(payment);
    }

    public PaymentDTO mapToDTO(Payment p) {
        LocalDate today = LocalDate.now();
        PaymentStatus derivedStatus = p.getDerivedStatus(today);
        boolean isLate = (derivedStatus == PaymentStatus.OVERDUE);
        long lateDays = isLate ? ChronoUnit.DAYS.between(p.getDueDate(), today) : 0;

        return PaymentDTO.builder()
                .id(p.getId())
                .loanId(p.getLoan().getId())
                .borrowerId(p.getLoan().getBorrower().getId())
                .borrowerName(p.getLoan().getBorrower().getFullName())
                .installmentNumber(p.getInstallmentNumber())
                .dueDate(p.getDueDate())
                .expectedAmount(p.getExpectedAmount())
                .paidAmount(p.getPaidAmount())
                .paidDate(p.getPaidDate())
                .status(derivedStatus)
                .late(isLate)
                .lateDays(lateDays)
                .notes(p.getNotes())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
