package com.n26.challenge.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class Transaction {

    @NotNull
    private final Double amount;

    @NotNull
    private final Long timestamp;
}
