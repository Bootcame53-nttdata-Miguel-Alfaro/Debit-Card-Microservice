package com.nttdata.bank.debit_card.service;

import com.nttdata.bank.debit_card.domain.Operation;
import com.nttdata.bank.debit_card.domain.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {
    Mono<Transaction> deposit(String cardNumber, Mono<Operation> operation);
    Mono<Transaction> withdraw(String cardNumber, Mono<Operation> operation);
    Flux<Transaction> getTransactions(String debitCardNumber);
}