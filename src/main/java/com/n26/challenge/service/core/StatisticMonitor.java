package com.n26.challenge.service.core;

import com.n26.challenge.exception.NonSubmittableTransactionTimestampException;
import com.n26.challenge.model.Statistic;
import com.n26.challenge.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
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

    private final Queue<Transaction> transactions = new PriorityQueue(Comparator.comparing(Transaction::getTimestamp));

    @PostConstruct
    public void initMonitor() {
        executorService.scheduleWithFixedDelay(() -> {
            if (!transactions.isEmpty()) {
                clearOutdatedTransactions();
            }
            recalculateStats();
        }, 0, 1, TimeUnit.MILLISECONDS);
    }

    public Statistic getStatistic() {
        return new Statistic(sum, avg, max, min, count);
    }

    public void submitTransaction(Transaction transaction) {
        if (isDateOutdated(transaction) || !isTimeStampCorrect(transaction))
            throw new NonSubmittableTransactionTimestampException();
        transactions.add(transaction);
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
            transactions.remove();
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
