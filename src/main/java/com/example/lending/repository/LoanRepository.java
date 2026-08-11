package com.example.lending.repository;

import com.example.lending.entity.Loan;
import com.example.lending.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    @Query("SELECT l FROM Loan l JOIN FETCH l.borrower b WHERE b.active = true")
    List<Loan> findAllWithActiveBorrowers();
    
    List<Loan> findByBorrowerId(Long borrowerId);
    
    List<Loan> findByBorrowerIdAndStatus(Long borrowerId, LoanStatus status);
    
    List<Loan> findByStatus(LoanStatus status);
    
    long countByStatus(LoanStatus status);
}
