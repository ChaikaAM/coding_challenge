package com.n26.challenge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticService {

    private final StatisticMonitor statisticMonitor;

    @Autowired
    public StatisticService(StatisticMonitor statisticMonitor) {
        this.statisticMonitor = statisticMonitor;
    }
}
