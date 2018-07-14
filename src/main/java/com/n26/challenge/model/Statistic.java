package com.n26.challenge.model;

import lombok.Data;

@Data
public class Statistic {
    private final double sum;
    private final double avg;
    private final double max;
    private final double min;
    private final long count;
}
