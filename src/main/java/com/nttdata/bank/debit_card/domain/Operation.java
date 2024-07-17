package com.nttdata.bank.debit_card.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Operation {
    private Double amount;
    private String description;
    public Operation(Double amount, String description){
        this.amount = amount;
        this.description = description;
    }
}