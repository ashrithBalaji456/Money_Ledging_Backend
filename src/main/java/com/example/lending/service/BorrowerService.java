package com.example.lending.service;

import com.example.lending.dto.BorrowerDTO;
import com.example.lending.entity.Borrower;
import com.example.lending.entity.Loan;
import com.example.lending.entity.LoanStatus;
import com.example.lending.exception.ResourceNotFoundException;
import com.example.lending.repository.BorrowerRepository;
import com.example.lending.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;
    private final LoanRepository loanRepository;

    @Transactional
    public BorrowerDTO createBorrower(BorrowerDTO dto) {
        Borrower borrower = Borrower.builder()
                .fullName(dto.getFullName())
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .aadharLastFourDigits(dto.getAadharLastFourDigits())
                .notes(dto.getNotes())
                .active(true)
                .build();
        
        Borrower saved = borrowerRepository.save(borrower);
        return mapToDTO(saved);
    }

    @Transactional
    public BorrowerDTO updateBorrower(Long id, BorrowerDTO dto) {
        Borrower borrower = borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found with id: " + id));
        
        borrower.setFullName(dto.getFullName());
        borrower.setPhoneNumber(dto.getPhoneNumber());
        borrower.setAddress(dto.getAddress());
        borrower.setAadharLastFourDigits(dto.getAadharLastFourDigits());
        borrower.setNotes(dto.getNotes());
        
        Borrower updated = borrowerRepository.save(borrower);
        return mapToDTO(updated);
    }

    @Transactional(readOnly = true)
    public List<BorrowerDTO> getActiveBorrowers(String query) {
        List<Borrower> borrowers;
        if (query == null || query.trim().isEmpty()) {
            borrowers = borrowerRepository.findByActiveTrue();
        } else {
            borrowers = borrowerRepository.searchActiveBorrowers(query.trim());
        }
        return borrowers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BorrowerDTO getBorrowerById(Long id) {
        Borrower borrower = borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found with id: " + id));
        return mapToDTO(borrower);
    }

    @Transactional
    public void deleteBorrower(Long id) {
        Borrower borrower = borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found with id: " + id));
        borrower.setActive(false);
        borrowerRepository.save(borrower);
    }

    public BorrowerDTO mapToDTO(Borrower b) {
        List<Loan> loans = loanRepository.findByBorrowerId(b.getId());
        
        int activeLoans = (int) loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.OVERDUE)
                .count();

        BigDecimal outstandingAmount = loans.stream()
                .filter(l -> l.getStatus() != LoanStatus.CANCELLED)
                .map(Loan::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate nextPaymentDate = loans.stream()
                .filter(l -> (l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.OVERDUE) && l.getNextPaymentDate() != null)
                .map(Loan::getNextPaymentDate)
                .min(LocalDate::compareTo)
                .orElse(null);

        LocalDate loanStartDate = loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.OVERDUE)
                .map(Loan::getLoanStartDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        return BorrowerDTO.builder()
                .id(b.getId())
                .fullName(b.getFullName())
                .phoneNumber(b.getPhoneNumber())
                .address(b.getAddress())
                .aadharLastFourDigits(b.getAadharLastFourDigits())
                .notes(b.getNotes())
                .active(b.isActive())
                .activeLoans(activeLoans)
                .outstandingAmount(outstandingAmount)
                .nextPaymentDate(nextPaymentDate)
                .loanStartDate(loanStartDate)
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
