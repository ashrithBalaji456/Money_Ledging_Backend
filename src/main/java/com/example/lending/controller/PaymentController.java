package com.example.lending.controller;

import com.example.lending.dto.PaymentDTO;
import com.example.lending.dto.PaymentRecordRequest;
import com.example.lending.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) {
        PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByLoanId(@PathVariable Long loanId) {
        List<PaymentDTO> payments = paymentService.getPaymentsByLoanId(loanId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/due-soon")
    public ResponseEntity<List<PaymentDTO>> getDueSoonPayments() {
        List<PaymentDTO> payments = paymentService.getDueSoonPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<PaymentDTO>> getOverduePayments() {
        List<PaymentDTO> payments = paymentService.getOverduePayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<PaymentDTO>> getRecentPayments() {
        List<PaymentDTO> payments = paymentService.getRecentPayments();
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentDTO> recordPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRecordRequest request
    ) {
        PaymentDTO updated = paymentService.recordPayment(id, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/partial-payment")
    public ResponseEntity<PaymentDTO> recordPartialPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRecordRequest request
    ) {
        PaymentDTO updated = paymentService.recordPartialPayment(id, request);
        return ResponseEntity.ok(updated);
    }
}
