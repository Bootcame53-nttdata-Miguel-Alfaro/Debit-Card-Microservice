package com.nttdata.bank.debit_card.service.impl;

import com.nttdata.bank.debit_card.domain.Operation;
import com.nttdata.bank.debit_card.domain.Transaction;
import com.nttdata.bank.debit_card.domain.ValidateRequest;
import com.nttdata.bank.debit_card.repository.DebitCardRepository;
import com.nttdata.bank.debit_card.repository.TransactionRepository;
import com.nttdata.bank.debit_card.service.AccountService;
import com.nttdata.bank.debit_card.service.TransactionService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final DebitCardRepository debitCardRepository;
    private final AccountService accountService;

    public TransactionServiceImpl(TransactionRepository transactionRepository, DebitCardRepository debitCardRepository, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.debitCardRepository = debitCardRepository;
        this.accountService = accountService;
    }

    @Override
    public Mono<Transaction> deposit(String cardNumber, Mono<Operation> operation) {
        return debitCardRepository.findByCardNumber(cardNumber)
                .flatMap(debitCard -> operation.flatMap(op -> validateAndDeposit(debitCard.getPrimaryAccountId(), cardNumber, op)))
                .switchIfEmpty(Mono.error(new RuntimeException("The import is not valid")))
                .doOnNext(transaction -> System.out.println("Transaction successful: " + transaction.getId()));
    }


    private Mono<Transaction> validateAndDeposit(String accountId, String cardNumber, Operation operation) {
        return accountService.validateBalance(accountId, new ValidateRequest("deposit", operation.getAmount()))
                .flatMap(validateResponse -> {
                    if (validateResponse.getSufficientBalance()) {
                        return accountService.deposit(accountId, operation)
                                .flatMap(transaction -> createTransaction("deposit",accountId, cardNumber, operation, transaction));
                    } else {
                        return Mono.empty();
                    }
                });
    }


    @Override
    public Mono<Transaction> withdraw(String cardNumber, Mono<Operation> operation) {
        Mono<Operation> cachedOperation = operation.cache(); // Tener la operacion en chache y asi no se pierda en la siguente busqueda

        return debitCardRepository.findByCardNumber(cardNumber)
                .flatMapMany(debitCard -> Flux.fromIterable(debitCard.getLinkedAccountIds()))
                .concatMap(accountId -> cachedOperation.flatMap(op -> validateAndWithdraw(accountId, cardNumber, op))
                        .onErrorResume(e -> {
                            System.out.println("Attempt failed for account " + accountId + ": " + e.getMessage());
                            return Mono.empty();
                        }))
                .next()
                .switchIfEmpty(Mono.error(new RuntimeException("No accounts with sufficient balance")))
                .doOnNext(transaction -> System.out.println("Transaction successful: " + transaction.getId()));
    }

    private Mono<Transaction> validateAndWithdraw(String accountId, String cardNumber, Operation operation) {
        return accountService.validateBalance(accountId, new ValidateRequest("withdraw", operation.getAmount()))
                .flatMap(validateResponse -> {
                    if (validateResponse.getSufficientBalance()) {
                        return accountService.withdraw(accountId, operation)
                                .flatMap(transaction -> createTransaction("withdraw", accountId, cardNumber, operation, transaction));
                    } else {
                        return Mono.error(new RuntimeException("Insufficient balance"));
                    }
                })
                .onErrorResume(e -> {
                    System.out.println("Attempt failed for account " + accountId + ": " + e.getMessage());
                    return Mono.empty();
                });
    }

    private Mono<Transaction> createTransaction(String type, String accountId, String cardNumber, Operation operation, Transaction transaction) {
        Transaction newTransaction = new Transaction();
        newTransaction.setAccountTransactionId(transaction.getId());
        newTransaction.setDebitCardNumber(cardNumber);
        newTransaction.setAmount(operation.getAmount());
        newTransaction.setDate(new Date());
        newTransaction.setType(type);
        newTransaction.setDescription(operation.getDescription());
        return transactionRepository.save(newTransaction);
    }

    @Override
    public Flux<Transaction> getTransactions(String debitCardNumber) {
        return transactionRepository.findByDebitCardNumber(debitCardNumber);
    }
}
