package com.n26.challenge;

import com.n26.challenge.exception.NonSubmittableTransactionTimestampException;
import com.n26.challenge.model.Statistic;
import com.n26.challenge.model.Transaction;
import com.n26.challenge.service.StatisticService;
import com.n26.challenge.service.TransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SimpleIntegrationTests {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private StatisticService statisticService;

    @Value("${application.transaction.outdate}")
    private Integer outdate;

    @Test(expected = NonSubmittableTransactionTimestampException.class)
    public void testWrongTimestampTransaction() {
        transactionService.handleTransaction(getWrongTimestampTransaction(2.0));
    }

    @Test
    public void testSingleTransaction() {
        Transaction singleTransaction = getCorrectTimestampTransaction(2.0);
        transactionService.handleTransaction(singleTransaction);
        Statistic statistic = statisticService.getStatistic();
        assertEquals("Avg should has the same value as in single transaction", Double.valueOf(statistic.getAvg()), singleTransaction.getAmount());
    }

    @Test
    public void testStatisticChangingInTime() {
        Transaction singleTransaction = getCorrectTimestampTransaction(3.0);
        transactionService.handleTransaction(singleTransaction);
        waitForSec(outdate + 1);
        Statistic statistic = statisticService.getStatistic();
        assertTrue("Transaction should be removed out of statistic after it has been outdated", statistic.getSum() < singleTransaction.getAmount());
    }

    @Test
    public void testStatisticChangingInTimeMultipleTransactions() {
        Transaction transaction1 = getCorrectTimestampTransaction(1.0);

        transactionService.handleTransaction(transaction1);
        waitForSec(outdate - 2);

        Transaction transaction2 = getCorrectTimestampTransaction(5.0);
        transactionService.handleTransaction(transaction2);
        assertTrue("Transaction statistic should contain info about both transations", statisticService.getStatistic().getSum() == transaction1.getAmount() + transaction2.getAmount());

        waitForSec(3);
        assertTrue("After first transaction expired,  info about that should be removed from statistics", statisticService.getStatistic().getCount() == 1);

        waitForSec(outdate);
        assertTrue("After both transactions have been expired. statistics should not contain information about them", statisticService.getStatistic().getSum() == 0);
    }

    private Transaction getCorrectTimestampTransaction(double amount) {
        return new Transaction(amount, System.currentTimeMillis() - 5);
    }

    private Transaction getWrongTimestampTransaction(double amount) {
        return new Transaction(amount, System.currentTimeMillis() - (outdate * 1200));
    }

    private void waitForSec(int sec) {
        System.out.println(sec);
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

}
