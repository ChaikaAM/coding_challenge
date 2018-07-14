package com.n26.challenge.service;

import com.n26.challenge.exception.NonSubmittableTransactionTimestampException;
import com.n26.challenge.model.Transaction;
import com.n26.challenge.service.core.StatisticMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final StatisticMonitor statisticMonitor;

    @Autowired
    public TransactionService(StatisticMonitor statisticMonitor) {
        this.statisticMonitor = statisticMonitor;
    }

    public void handleTransaction(Transaction transaction) {
        if (!isTimestampCorrect(transaction)) throw new NonSubmittableTransactionTimestampException();
        statisticMonitor.submitTransaction(transaction);
    }

    private boolean isTimestampCorrect(Transaction transaction) {
        return transaction.getTimestamp().compareTo(System.currentTimeMillis()) < 0;
    }
}
