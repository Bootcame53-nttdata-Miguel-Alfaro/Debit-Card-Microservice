package com.nttdata.bank.debit_card.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {
    private String id;
    private String customerId;
    private String accountType;
    private String accountUsageType;
    private Double balance;
}