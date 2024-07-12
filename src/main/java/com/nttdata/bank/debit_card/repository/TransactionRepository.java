package com.nttdata.bank.debit_card.repository;

import com.nttdata.bank.debit_card.domain.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Flux<Transaction> findByDebitCardNumber(String debitCardNumber);
}