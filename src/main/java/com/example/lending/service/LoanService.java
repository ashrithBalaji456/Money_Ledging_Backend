package com.example.lending.service;

import com.example.lending.dto.LoanDTO;
import com.example.lending.dto.LoanCreationRequest;
import com.example.lending.dto.LoanPreviewResponse;
import com.example.lending.entity.Borrower;
import com.example.lending.entity.Loan;
import com.example.lending.entity.LoanStatus;
import com.example.lending.entity.Payment;
import com.example.lending.entity.PaymentStatus;
import com.example.lending.exception.ResourceNotFoundException;
import com.example.lending.repository.BorrowerRepository;
import com.example.lending.repository.LoanRepository;
import com.example.lending.repository.PaymentRepository;
import com.example.lending.entity.User;
import com.example.lending.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanService {

    private final LoanRepository loanRepository;
    private final BorrowerRepository borrowerRepository;
    private final PaymentRepository paymentRepository;
    private final InterestCalculationService interestCalculationService;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    public LoanDTO createLoan(LoanCreationRequest request) {
        User currentUser = getCurrentUser();
        Borrower borrower = borrowerRepository.findById(request.getBorrowerId())
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found with id: " + request.getBorrowerId()));

        if (borrower.getUser() == null || !borrower.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied to create loan for borrower: " + request.getBorrowerId());
        }

        BigDecimal interestAmount;
        BigDecimal totalPayable;
        BigDecimal monthlyInstallment;
        BigDecimal interestRateVal = request.getInterestRate();
        com.example.lending.entity.InterestRateType rateTypeVal = request.getInterestRateType();

        if (request.getInterestType() == com.example.lending.entity.InterestType.FIXED_INTEREST) {
            interestAmount = request.getCustomInterestAmount() != null ? request.getCustomInterestAmount() : BigDecimal.ZERO;
            totalPayable = request.getPrincipalAmount().add(interestAmount);
            monthlyInstallment = totalPayable.divide(BigDecimal.valueOf(request.getDurationInMonths()), 2, java.math.RoundingMode.HALF_UP);
            if (interestRateVal == null) {
                interestRateVal = BigDecimal.ZERO;
            }
            if (rateTypeVal == null) {
                rateTypeVal = com.example.lending.entity.InterestRateType.ANNUAL;
            }
        } else if (request.getInterestType() == com.example.lending.entity.InterestType.INTEREST_ONLY) {
            InterestCalculationService.CalculationResult calc = interestCalculationService.calculateInterestOnly(
                    request.getPrincipalAmount(),
                    request.getInterestRate(),
                    request.getInterestRateType(),
                    request.getDurationInMonths()
            );
            interestAmount = calc.interestAmount;
            totalPayable = calc.totalPayable;
            monthlyInstallment = calc.monthlyInstallment;
        } else {
            InterestCalculationService.CalculationResult calc = interestCalculationService.calculateSimpleInterest(
                    request.getPrincipalAmount(),
                    request.getInterestRate(),
                    request.getInterestRateType(),
                    request.getDurationInMonths()
            );
            interestAmount = calc.interestAmount;
            totalPayable = calc.totalPayable;
            monthlyInstallment = calc.monthlyInstallment;
        }

        Loan loan = Loan.builder()
                .borrower(borrower)
                .principalAmount(request.getPrincipalAmount())
                .interestRate(interestRateVal)
                .interestType(request.getInterestType())
                .interestRateType(rateTypeVal)
                .loanStartDate(request.getLoanStartDate())
                .durationInMonths(request.getDurationInMonths())
                .totalInterest(interestAmount)
                .totalPayableAmount(totalPayable)
                .monthlyInstallment(monthlyInstallment)
                .remainingPrincipal(request.getPrincipalAmount())
                .remainingAmount(totalPayable)
                .status(LoanStatus.ACTIVE)
                .notes(request.getNotes())
                .build();

        Loan savedLoan = loanRepository.save(loan);

        // Generate Installment Schedule
        List<Payment> installments = new ArrayList<>();
        BigDecimal accumulatedInstallments = BigDecimal.ZERO;

        for (int i = 1; i <= request.getDurationInMonths(); i++) {
            LocalDate dueDate = request.getLoanStartDate().plusMonths(i);
            BigDecimal expected;

            if (i == request.getDurationInMonths()) {
                // Adjust final installment to match total payable exactly
                expected = totalPayable.subtract(accumulatedInstallments);
            } else {
                expected = monthlyInstallment;
                accumulatedInstallments = accumulatedInstallments.add(expected);
            }

            Payment payment = Payment.builder()
                    .loan(savedLoan)
                    .installmentNumber(i)
                    .dueDate(dueDate)
                    .expectedAmount(expected)
                    .paidAmount(BigDecimal.ZERO)
                    .status(PaymentStatus.UPCOMING)
                    .build();
            
            installments.add(payment);
        }

        paymentRepository.saveAll(installments);

        // Set next payment date
        if (!installments.isEmpty()) {
            savedLoan.setNextPaymentDate(installments.get(0).getDueDate());
            loanRepository.save(savedLoan);
        }

        return mapToDTO(savedLoan);
    }

    @Transactional(readOnly = true)
    public LoanPreviewResponse previewLoan(LoanCreationRequest request) {
        BigDecimal interestAmount;
        BigDecimal totalPayable;
        BigDecimal monthlyInstallment;

        if (request.getInterestType() == com.example.lending.entity.InterestType.FIXED_INTEREST) {
            interestAmount = request.getCustomInterestAmount() != null ? request.getCustomInterestAmount() : BigDecimal.ZERO;
            totalPayable = request.getPrincipalAmount().add(interestAmount);
            monthlyInstallment = totalPayable.divide(BigDecimal.valueOf(request.getDurationInMonths()), 2, java.math.RoundingMode.HALF_UP);
        } else if (request.getInterestType() == com.example.lending.entity.InterestType.INTEREST_ONLY) {
            InterestCalculationService.CalculationResult calc = interestCalculationService.calculateInterestOnly(
                    request.getPrincipalAmount(),
                    request.getInterestRate(),
                    request.getInterestRateType(),
                    request.getDurationInMonths()
            );
            interestAmount = calc.interestAmount;
            totalPayable = calc.totalPayable;
            monthlyInstallment = calc.monthlyInstallment;
        } else {
            InterestCalculationService.CalculationResult calc = interestCalculationService.calculateSimpleInterest(
                    request.getPrincipalAmount(),
                    request.getInterestRate(),
                    request.getInterestRateType(),
                    request.getDurationInMonths()
            );
            interestAmount = calc.interestAmount;
            totalPayable = calc.totalPayable;
            monthlyInstallment = calc.monthlyInstallment;
        }

        return LoanPreviewResponse.builder()
                .principalAmount(request.getPrincipalAmount())
                .totalInterest(interestAmount)
                .totalPayableAmount(totalPayable)
                .monthlyInstallment(monthlyInstallment)
                .build();
    }

    @Transactional(readOnly = true)
    public List<LoanDTO> getLoans(LoanStatus status) {
        User currentUser = getCurrentUser();
        List<Loan> loans = loanRepository.findAllWithActiveBorrowers(currentUser.getId());
        if (status != null) {
            loans = loans.stream().filter(l -> l.getStatus() == status).collect(Collectors.toList());
        }
        return loans.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LoanDTO getLoanById(Long id) {
        User currentUser = getCurrentUser();
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));
        if (loan.getBorrower().getUser() == null || !loan.getBorrower().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied to view loan: " + id);
        }
        return mapToDTO(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanDTO> getLoansByBorrowerId(Long borrowerId) {
        User currentUser = getCurrentUser();
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found with id: " + borrowerId));
        if (borrower.getUser() == null || !borrower.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied to view loans for borrower: " + borrowerId);
        }
        return loanRepository.findByBorrowerId(borrowerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public LoanDTO mapToDTO(Loan loan) {
        return LoanDTO.builder()
                .id(loan.getId())
                .borrowerId(loan.getBorrower().getId())
                .borrowerName(loan.getBorrower().getFullName())
                .principalAmount(loan.getPrincipalAmount())
                .interestRate(loan.getInterestRate())
                .interestType(loan.getInterestType())
                .interestRateType(loan.getInterestRateType())
                .loanStartDate(loan.getLoanStartDate())
                .durationInMonths(loan.getDurationInMonths())
                .totalInterest(loan.getTotalInterest())
                .totalPayableAmount(loan.getTotalPayableAmount())
                .monthlyInstallment(loan.getMonthlyInstallment())
                .remainingPrincipal(loan.getRemainingPrincipal())
                .remainingAmount(loan.getRemainingAmount())
                .nextPaymentDate(loan.getNextPaymentDate())
                .status(loan.getStatus())
                .notes(loan.getNotes())
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }
}
