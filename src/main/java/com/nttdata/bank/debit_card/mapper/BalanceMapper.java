package com.nttdata.bank.debit_card.mapper;

import com.nttdata.bank.debit_card.domain.Account;
import com.nttdata.bank.debit_card.model.AccountBalance;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class BalanceMapper implements EntityMapper<AccountBalance, Account>{
    @Override
    public Account toDomain(AccountBalance model) {
        Account account = new Account();
        BeanUtils.copyProperties(model, account);
        return account;
    }

    @Override
    public AccountBalance toModel(Account domain) {
        AccountBalance accountBalance = new AccountBalance();
        BeanUtils.copyProperties(domain, accountBalance);
        return accountBalance;
    }
}
