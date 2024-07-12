package com.nttdata.bank.debit_card.mapper;

import com.nttdata.bank.debit_card.domain.Transaction;
import com.nttdata.bank.debit_card.model.TransactionResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper implements EntityMapper<TransactionResponse, Transaction> {
    @Override
    public Transaction toDomain(TransactionResponse model) {
        Transaction transaction = new Transaction();
        BeanUtils.copyProperties(model, transaction);
        return transaction;
    }

    @Override
    public TransactionResponse toModel(Transaction domain) {
        TransactionResponse model = new TransactionResponse();
        BeanUtils.copyProperties(domain, model);
        return model;
    }
}
