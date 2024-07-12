package com.nttdata.bank.debit_card.controller;

import com.nttdata.bank.debit_card.api.DebitCardsApi;
import com.nttdata.bank.debit_card.mapper.BalanceMapper;
import com.nttdata.bank.debit_card.mapper.DebitCardMapper;
import com.nttdata.bank.debit_card.mapper.OperationMapper;
import com.nttdata.bank.debit_card.mapper.TransactionMapper;
import com.nttdata.bank.debit_card.model.AccountBalance;
import com.nttdata.bank.debit_card.model.DebitCard;

import com.nttdata.bank.debit_card.model.TransactionOperation;
import com.nttdata.bank.debit_card.model.TransactionResponse;
import com.nttdata.bank.debit_card.service.DebitCardService;
import com.nttdata.bank.debit_card.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class DebitCardController implements DebitCardsApi {

    private final DebitCardService debitCardService;
    private final TransactionService transactionService;
    private final DebitCardMapper debitCardMapper;
    private final TransactionMapper transactionMapper;
    private final OperationMapper operationMapper;
    private final BalanceMapper balanceMapper;

    public DebitCardController(DebitCardService debitCardService, TransactionService transactionService, DebitCardMapper debitCardMapper, TransactionMapper transactionMapper, OperationMapper operationMapper, BalanceMapper balanceMapper) {
        this.debitCardService = debitCardService;
        this.transactionService = transactionService;
        this.debitCardMapper = debitCardMapper;
        this.transactionMapper = transactionMapper;
        this.operationMapper = operationMapper;
        this.balanceMapper = balanceMapper;
    }

    @Override
    public Mono<ResponseEntity<AccountBalance>> accountBalance(String cardId, ServerWebExchange exchange) {
        return debitCardService.getBalance(cardId)
                .map(balanceMapper::toModel)
                .map(c -> ResponseEntity.status(HttpStatus.OK).body(c))
        .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)));
    }

    @Override
    public Mono<ResponseEntity<DebitCard>> createDebitCard(Mono<DebitCard> debitCard, ServerWebExchange exchange) {
        return debitCardService.save(debitCard.map(debitCardMapper::toDomain))
                .map(debitCardMapper::toModel)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteDebitCard(String id, ServerWebExchange exchange) {
        return debitCardService.findById(id)
                .flatMap(c -> debitCardService.delete(id)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<DebitCard>> getDebitCardById(String id, ServerWebExchange exchange) {
        return debitCardService.findById(id)
                .map(debitCardMapper::toModel)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<DebitCard>> linkAccount(String cardId, String accountId, ServerWebExchange exchange) {
        return debitCardService.linkAccount(cardId, accountId)
                .map(debitCardMapper::toModel)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<DebitCard>> setPrimaryAccount(String cardId, String primaryAccountId, ServerWebExchange exchange) {
        return debitCardService.setPrimaryAccount(cardId, primaryAccountId)
                .map(debitCardMapper::toModel)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> withdrawFromDebitCard(String id, Mono<TransactionOperation> transactionOperation, ServerWebExchange exchange) {
        return transactionService.withdraw(id, transactionOperation.map(operationMapper::toDomain))
                .map(transactionMapper::toModel)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c));
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> depositToDebitCard(String id, Mono<TransactionOperation> transactionOperation, ServerWebExchange exchange) {
        return transactionService.deposit(id, transactionOperation.map(operationMapper::toDomain))
                .map(transactionMapper::toModel)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c));
    }

    @Override
    public Mono<ResponseEntity<Flux<TransactionResponse>>> getDebitCardTransactions(String id, ServerWebExchange exchange) {
        Flux<TransactionResponse> creditsFlux = transactionService.getTransactions(id)
                .map(transactionMapper::toModel);

        return Mono.just(ResponseEntity.ok(creditsFlux))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
