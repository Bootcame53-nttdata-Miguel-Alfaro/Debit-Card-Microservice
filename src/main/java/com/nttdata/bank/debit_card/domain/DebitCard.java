package com.nttdata.bank.debit_card.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "debit-card")
public class DebitCard {
    @Id
    private String id;
    private String cardNumber;
    private String customerId;
    private String primaryAccountId;
    private List<String> linkedAccountIds = new ArrayList<>();
    @CreatedDate
    private Date createdAt;
}