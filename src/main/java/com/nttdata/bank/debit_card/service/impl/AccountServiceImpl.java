package com.nttdata.bank.debit_card.service.impl;

import com.nttdata.bank.debit_card.domain.*;
import com.nttdata.bank.debit_card.service.AccountService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccountServiceImpl implements AccountService {
    private final WebClient.Builder webClientBuilder;

    public AccountServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Flux<Account> findAccountsByCustomerId(String customerId) {
        return webClientBuilder.build()
                .get()
                .uri("/accounts/customer/" + customerId)
                .retrieve()
                .bodyToFlux(Account.class);
    }

    @Override
    public Mono<Account> findMainAccountBalance(String accountId) {
        return webClientBuilder.build()
                .get()
                .uri("/accounts/{id}", accountId)
                .retrieve()
                .bodyToMono(Account.class);
    }

    @Override
    public Mono<ValidateResponse> validateBalance(String accountId, ValidateRequest request) {
        return webClientBuilder.build()
                .post()
                .uri("/accounts/{id}/balance/validate", accountId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ValidateResponse.class);
    }

    @Override
    public Mono<Transaction> deposit(String accountId, Operation operation) {
        return webClientBuilder.build()
                .post()
                .uri("/accounts/{id}/deposit", accountId)
                .bodyValue(operation)
                .retrieve()
                .bodyToMono(Transaction.class);
    }

    @Override
    public Mono<Transaction> withdraw(String accountId, Operation operation) {
        return webClientBuilder.build()
                .post()
                .uri("/accounts/{id}/withdraw", accountId)
                .bodyValue(operation)
                .retrieve()
                .bodyToMono(Transaction.class);
    }
}
