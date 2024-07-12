package com.nttdata.bank.debit_card.service;

import com.nttdata.bank.debit_card.domain.Account;
import com.nttdata.bank.debit_card.domain.DebitCard;
import reactor.core.publisher.Mono;

public interface DebitCardService {
    Mono<DebitCard> save(Mono<DebitCard> debitCard);
    Mono<DebitCard> linkAccount(String cardId, String accountId);
    Mono<DebitCard> setPrimaryAccount(String cardId, String primaryAccountId);
    Mono<DebitCard> findById(String cardId);
    Mono<Void> delete(String cardId);
    Mono<Account> getBalance(String cardNumber);
}