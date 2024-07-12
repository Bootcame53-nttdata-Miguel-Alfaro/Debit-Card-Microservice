package com.nttdata.bank.debit_card.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ValidateResponse {
    private String status;
    private Boolean sufficientBalance;

    public ValidateResponse(String status, Boolean sufficientBalance) {
        this.status = status;
        this.sufficientBalance = sufficientBalance;
    }
}