package com.nttdata.bank.debit_card.service;

import com.nttdata.bank.debit_card.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {
    Flux<Account> findAccountsByCustomerId(String customerId);
    Mono<Account> findMainAccountBalance(String accountId);
    Mono<ValidateResponse> validateBalance(String accountId, ValidateRequest request);
    Mono<Transaction> deposit(String accountId, Operation operation);
    Mono<Transaction> withdraw(String accountId, Operation operation);
}