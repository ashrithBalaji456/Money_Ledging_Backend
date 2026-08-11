package com.example.lending.controller;

import com.example.lending.dto.BorrowerDTO;
import com.example.lending.service.BorrowerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowers")
@RequiredArgsConstructor
public class BorrowerController {

    private final BorrowerService borrowerService;

    @PostMapping
    public ResponseEntity<BorrowerDTO> createBorrower(@Valid @RequestBody BorrowerDTO dto) {
        BorrowerDTO created = borrowerService.createBorrower(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BorrowerDTO>> getBorrowers(@RequestParam(value = "query", required = false) String query) {
        List<BorrowerDTO> borrowers = borrowerService.getActiveBorrowers(query);
        return ResponseEntity.ok(borrowers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowerDTO> getBorrowerById(@PathVariable Long id) {
        BorrowerDTO borrower = borrowerService.getBorrowerById(id);
        return ResponseEntity.ok(borrower);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BorrowerDTO> updateBorrower(@PathVariable Long id, @Valid @RequestBody BorrowerDTO dto) {
        BorrowerDTO updated = borrowerService.updateBorrower(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBorrower(@PathVariable Long id) {
        borrowerService.deleteBorrower(id);
        return ResponseEntity.noContent().build();
    }
}
