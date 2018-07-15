package com.n26.challenge.service.core;

import com.n26.challenge.exception.NonSubmittableTransactionTimestampException;
import com.n26.challenge.model.Statistic;
import com.n26.challenge.model.Transaction;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
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

    private final PriorityBlockingQueue<Transaction> transactions = new PriorityBlockingQueue<>(1, Comparator.comparing(Transaction::getTimestamp));

    @PostConstruct
    public void initMonitor() {
        log.info("Starting statistics monitor (handles statistics of transactions not older than {} secs)", outdate);
        executorService.scheduleWithFixedDelay(() -> {
            if (transactions.isEmpty()) {
                waitForAnyTransaction();
                log.info("Handling submitted transactions");
            }
            clearOutdatedTransactions();
            recalculateStats();
        }, 0, 1, TimeUnit.MILLISECONDS);
    }

    private void waitForAnyTransaction() {
        log.info("Waiting for any transaction");
        try {
            transactions.add(transactions.take());    //block until any transaction will be added
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public Statistic getStatistic() {
        return new Statistic(sum, avg, max, min, count);
    }

    public void submitTransaction(Transaction transaction) {
        log.info("Transaction received - {}", transaction);
        if (isDateOutdated(transaction) || !isTimeStampCorrect(transaction)) {
            log.error("Transaction is out of date or timestamp has wrong format");
            throw new NonSubmittableTransactionTimestampException();
        }
        transactions.add(transaction);
        log.info("Transaction added to statistics handling - {}", transaction);
        recalculateStats();
    }

    private boolean isDateOutdated(Transaction transaction) {
        return System.currentTimeMillis() - transaction.getTimestamp() > outdate * 1000;
    }

    private boolean isTimeStampCorrect(Transaction transaction) {
        return transaction.getTimestamp().compareTo(System.currentTimeMillis()) < 0;
    }

    private synchronized void clearOutdatedTransactions() {
        while (!transactions.isEmpty() && isDateOutdated(transactions.peek())) {
            Transaction outdatedTransaction = transactions.remove();
            log.info("Transaction has been removed as outdated - {}", outdatedTransaction);
        }
    }

    private synchronized void recalculateStats() {
        if (transactions.isEmpty()) {
            sum = 0;
            avg = 0;
            max = 0;
            min = 0;
            count = 0;
        } else {
            sum = transactions.stream().mapToDouble(Transaction::getAmount).sum();
            avg = transactions.stream().mapToDouble(Transaction::getAmount).average().orElse(0);
            max = transactions.stream().mapToDouble(Transaction::getAmount).max().orElse(0);
            min = transactions.stream().mapToDouble(Transaction::getAmount).min().orElse(0);
            count = transactions.size();
        }
    }

    @PreDestroy
    public void shutDown() {
        executorService.shutdown();
    }
}
