package com.nttdata.bank.debit_card.mapper;

import com.nttdata.bank.debit_card.model.DebitCard;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class DebitCardMapper implements EntityMapper<DebitCard, com.nttdata.bank.debit_card.domain.DebitCard> {
    @Override
    public com.nttdata.bank.debit_card.domain.DebitCard toDomain(DebitCard model) {
        com.nttdata.bank.debit_card.domain.DebitCard domain = new com.nttdata.bank.debit_card.domain.DebitCard();
        BeanUtils.copyProperties(model, domain);
        return domain;
    }

    @Override
    public DebitCard toModel(com.nttdata.bank.debit_card.domain.DebitCard domain) {
        DebitCard model = new DebitCard();
        BeanUtils.copyProperties(domain, model);
        return model;
    }
}
