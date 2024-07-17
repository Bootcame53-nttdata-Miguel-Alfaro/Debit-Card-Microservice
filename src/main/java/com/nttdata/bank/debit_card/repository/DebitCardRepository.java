package com.nttdata.bank.debit_card.repository;

import com.nttdata.bank.debit_card.domain.DebitCard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DebitCardRepository extends ReactiveMongoRepository<DebitCard, String> {
    Mono<DebitCard> findByCardNumber(String cardNumber);
    Flux<DebitCard> findAllByCardNumber(String cardNumber);
}