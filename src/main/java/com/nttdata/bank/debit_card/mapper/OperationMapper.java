package com.nttdata.bank.debit_card.mapper;

import com.nttdata.bank.debit_card.domain.Operation;
import com.nttdata.bank.debit_card.model.TransactionOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class OperationMapper implements EntityMapper<TransactionOperation, Operation> {
    @Override
    public Operation toDomain(TransactionOperation model) {
        Operation operation = new Operation();
        BeanUtils.copyProperties(model, operation);
        return operation;
    }

    @Override
    public TransactionOperation toModel(Operation domain) {
        TransactionOperation operation = new TransactionOperation();
        BeanUtils.copyProperties(domain, operation);
        return operation;
    }
}
