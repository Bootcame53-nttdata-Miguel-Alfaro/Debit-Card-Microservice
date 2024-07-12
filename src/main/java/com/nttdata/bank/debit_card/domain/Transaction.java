package com.nttdata.bank.debit_card.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "transaction")
public class Transaction {
    @Id
    private String id;
    private String accountTransactionId;
    private String debitCardNumber;
    private Double amount;
    private Date date;
    private String type;
    private String description;
}