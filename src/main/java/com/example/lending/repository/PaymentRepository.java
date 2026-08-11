package com.example.lending.repository;

import com.example.lending.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByLoanIdOrderByInstallmentNumberAsc(Long loanId);
    
    // Find all unpaid payments that are overdue (dueDate < date)
    @Query("SELECT p FROM Payment p JOIN FETCH p.loan l JOIN FETCH l.borrower b " +
           "WHERE b.active = true AND b.user.id = :userId AND p.dueDate < :date AND p.paidAmount < p.expectedAmount " +
           "ORDER BY p.dueDate ASC")
    List<Payment> findOverduePayments(@Param("date") LocalDate date, @Param("userId") Long userId);
    
    // Find all unpaid payments due between date and date + 3 days (inclusive)
    @Query("SELECT p FROM Payment p JOIN FETCH p.loan l JOIN FETCH l.borrower b " +
           "WHERE b.active = true AND b.user.id = :userId AND p.dueDate >= :startDate AND p.dueDate <= :endDate AND p.paidAmount < p.expectedAmount " +
           "ORDER BY p.dueDate ASC")
    List<Payment> findPaymentsDueSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("userId") Long userId);
    
    // Find recent payments (paidDate is not null, ordered by paidDate DESC)
    @Query("SELECT p FROM Payment p JOIN FETCH p.loan l JOIN FETCH l.borrower b " +
           "WHERE b.active = true AND b.user.id = :userId AND p.paidDate IS NOT NULL " +
           "ORDER BY p.paidDate DESC, p.updatedAt DESC")
    List<Payment> findRecentPayments(@Param("userId") Long userId);
}
