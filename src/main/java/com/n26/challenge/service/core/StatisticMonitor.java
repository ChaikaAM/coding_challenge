package com.n26.challenge.service.core;

import com.n26.challenge.exception.NonSubmittableTransactionTimestampException;
import com.n26.challenge.model.Statistic;
import com.n26.challenge.model.Transaction;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class StatisticMonitor {

    @Value("${application.transaction.outdate}")
    private Long outdate;

    private final ScheduledExecutorService executorService =
            Executors.newSingleThreadScheduledExecutor();

    private volatile double sum = 0;
    private volatile double avg = 0;
    private volatile double max = 0;
    private volatile double min = 0;
    private volatile long count = 0;

    private Long idCounter = Long.MIN_VALUE;
    private final Map<Long, Transaction> transactions = new HashMap<>();

    public synchronized void submitTransaction(Transaction transaction) {
        log.info("Transaction received - {}", transaction);
        if (isDateOutdated(transaction) || !isTimeStampCorrect(transaction)) {
            log.error("Transaction is out of date or timestamp has wrong format");
            throw new NonSubmittableTransactionTimestampException();
        }
        Long transationID = ++idCounter;
        transactions.put(transationID, transaction);
        scheduleTransactionRemoving(transationID, transaction);
        log.info("Transaction added to statistics handling - {}", transaction);
        recalculateStats();
    }

    public synchronized Statistic getStatistic() {
        return new Statistic(sum, avg, max, min, count);
    }

    private boolean isDateOutdated(Transaction transaction) {
        return System.currentTimeMillis() - transaction.getTimestamp() > outdate * 1000;
    }

    private boolean isTimeStampCorrect(Transaction transaction) {
        return transaction.getTimestamp().compareTo(System.currentTimeMillis()) < 0;
    }

    private void scheduleTransactionRemoving(Long transationID, Transaction transaction) {
        executorService.schedule(() -> {
            transactions.remove(transationID);
            log.info("Transaction has been removed as outdated - {}", transaction);
            recalculateStats();
        }, outdate * 1000L - (System.currentTimeMillis() - transaction.getTimestamp()), TimeUnit.MILLISECONDS);
    }

    private synchronized void recalculateStats() {
        if (transactions.isEmpty()) {
            sum = 0;
            avg = 0;
            max = 0;
            min = 0;
            count = 0;
        } else {
            sum = transactions.values().stream().mapToDouble(Transaction::getAmount).sum();
            avg = transactions.values().stream().mapToDouble(Transaction::getAmount).average().orElse(0);
            max = transactions.values().stream().mapToDouble(Transaction::getAmount).max().orElse(0);
            min = transactions.values().stream().mapToDouble(Transaction::getAmount).min().orElse(0);
            count = transactions.size();
        }
    }

    @PreDestroy
    public void shutDown() {
        executorService.shutdown();
    }
}
