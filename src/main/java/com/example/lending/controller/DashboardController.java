package com.example.lending.controller;

import com.example.lending.dto.DashboardSummaryDTO;
import com.example.lending.dto.PaymentDTO;
import com.example.lending.service.DashboardService;
import com.example.lending.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final PaymentService paymentService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary() {
        DashboardSummaryDTO summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
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

    @GetMapping("/recent-payments")
    public ResponseEntity<List<PaymentDTO>> getRecentPayments() {
        List<PaymentDTO> payments = paymentService.getRecentPayments();
        return ResponseEntity.ok(payments);
    }
}
