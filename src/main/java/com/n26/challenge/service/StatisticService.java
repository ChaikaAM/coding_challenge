package com.n26.challenge.service;

import com.n26.challenge.model.Statistic;
import com.n26.challenge.service.core.StatisticMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticService {

    private final StatisticMonitor statisticMonitor;

    @Autowired
    public StatisticService(StatisticMonitor statisticMonitor) {
        this.statisticMonitor = statisticMonitor;
    }

    public Statistic getStatistic() {
        return statisticMonitor.getStatistic();
    }
}
