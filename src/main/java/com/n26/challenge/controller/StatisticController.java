package com.n26.challenge.controller;

import com.n26.challenge.model.Statistic;
import com.n26.challenge.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/statistics")
public class StatisticController {

    private final StatisticService statisticService;

    @Autowired
    public StatisticController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @GetMapping
    public Statistic getStatistics() {
        return statisticService.getStatistic();
    }
}
