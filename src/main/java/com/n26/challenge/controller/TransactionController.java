package com.n26.challenge.controller;

import com.n26.challenge.model.Transaction;
import com.n26.challenge.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final ResponseEntity successHandlingStatus;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
        successHandlingStatus = new ResponseEntity(HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity handleTransaction(@RequestBody @Valid Transaction transaction) {
        transactionService.handleTransaction(transaction);
        return successHandlingStatus;
    }
}
