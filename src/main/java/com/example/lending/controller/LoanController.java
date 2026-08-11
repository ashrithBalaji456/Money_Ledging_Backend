package com.example.lending.controller;

import com.example.lending.dto.LoanDTO;
import com.example.lending.dto.LoanCreationRequest;
import com.example.lending.dto.LoanPreviewResponse;
import com.example.lending.entity.LoanStatus;
import com.example.lending.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanDTO> createLoan(@Valid @RequestBody LoanCreationRequest request) {
        LoanDTO created = loanService.createLoan(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/preview")
    public ResponseEntity<LoanPreviewResponse> previewLoan(@Valid @RequestBody LoanCreationRequest request) {
        LoanPreviewResponse preview = loanService.previewLoan(request);
        return ResponseEntity.ok(preview);
    }

    @GetMapping
    public ResponseEntity<List<LoanDTO>> getLoans(@RequestParam(value = "status", required = false) LoanStatus status) {
        List<LoanDTO> loans = loanService.getLoans(status);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanDTO> getLoanById(@PathVariable Long id) {
        LoanDTO loan = loanService.getLoanById(id);
        return ResponseEntity.ok(loan);
    }

    @GetMapping("/borrower/{borrowerId}")
    public ResponseEntity<List<LoanDTO>> getLoansByBorrowerId(@PathVariable Long borrowerId) {
        List<LoanDTO> loans = loanService.getLoansByBorrowerId(borrowerId);
        return ResponseEntity.ok(loans);
    }
}
