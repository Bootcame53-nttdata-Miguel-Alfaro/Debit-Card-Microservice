package com.nttdata.bank.debit_card.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ValidateRequest {
    private String type;
    private Double balance;

    public ValidateRequest(String deposit, Double amount) {
        this.type = deposit;
        this.balance = amount;
    }
}