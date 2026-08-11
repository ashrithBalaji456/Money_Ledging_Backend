package com.example.lending.repository;

import com.example.lending.entity.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
    
    List<Borrower> findByActiveTrueAndUserId(Long userId);
    
    @Query("SELECT b FROM Borrower b WHERE b.active = true AND b.user.id = :userId AND " +
           "(LOWER(b.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "b.phoneNumber LIKE CONCAT('%', :query, '%'))")
    List<Borrower> searchActiveBorrowers(@Param("query") String query, @Param("userId") Long userId);
}
