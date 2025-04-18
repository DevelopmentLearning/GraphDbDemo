package org.culmanu.graph.controllers;

import org.culmanu.graph.services.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transaction")
    public ResponseEntity<String> makeTransaction(@RequestParam String from,
                                                  @RequestParam String to,
                                                  @RequestParam double amount) {
        try {
            transactionService.transferSequentially(from, to, amount);
            return ResponseEntity.ok("Transaction completed");
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body("Error: " + ex.getMessage());
        }
    }
}