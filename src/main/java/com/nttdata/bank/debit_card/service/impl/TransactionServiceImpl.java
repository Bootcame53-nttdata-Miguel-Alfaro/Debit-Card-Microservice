package com.nttdata.bank.debit_card.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.bank.debit_card.domain.MessageKafka;
import com.nttdata.bank.debit_card.domain.Operation;
import com.nttdata.bank.debit_card.domain.Transaction;
import com.nttdata.bank.debit_card.domain.ValidateRequest;
import com.nttdata.bank.debit_card.repository.DebitCardRepository;
import com.nttdata.bank.debit_card.repository.TransactionRepository;
import com.nttdata.bank.debit_card.service.AccountService;
import com.nttdata.bank.debit_card.service.TransactionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final DebitCardRepository debitCardRepository;
    private final AccountService accountService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String REQUEST_TOPIC_WITHDRAW = "transaction_withdraw_request";
    private static final String RESPONSE_TOPIC_WITHDRAW = "transaction_withdraw_response";

    public TransactionServiceImpl(TransactionRepository transactionRepository, DebitCardRepository debitCardRepository, AccountService accountService, KafkaTemplate<String, String> kafkaTemplate) {
        this.transactionRepository = transactionRepository;
        this.debitCardRepository = debitCardRepository;
        this.accountService = accountService;
        this.kafkaTemplate = kafkaTemplate;
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
                            System.out.println("More detail" + accountId + " : " + cardNumber);
                            System.out.println("Attempt failed for account " + accountId + ": " + e.getMessage());
                            return Mono.empty();
                        }))
                .next()
                .switchIfEmpty(Mono.error(new RuntimeException("No accounts with sufficient balance")))
                .doOnNext(transaction -> System.out.println("Transaction successful: " + transaction.getId()));
    }

    private Mono<Transaction> validateAndWithdraw(String accountId, String cardNumber, Operation operation) {
        System.out.println("More detail validate" + operation.getAmount() + " : " + operation.getDescription());
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

    @KafkaListener(topics = REQUEST_TOPIC_WITHDRAW, groupId = "debit_card_service_group")
    public void listen_withdraw(String messageJson) {
        System.out.println("Solicitud recibida de Kafka: " + messageJson);
        MessageKafka message;
        try {
            message = objectMapper.readValue(messageJson, MessageKafka.class);
        } catch (Exception e) {
            System.out.println("Fallo");
            throw new RuntimeException("Error deserializing MessageKafka", e);
        }
        String cardNumber = message.getInformation();
        Double cardValue = message.getValue();
        String correlationId = message.getCorrelationId();
        System.out.println("Buscando tarjeta con número: " + cardNumber);
        withdraw(cardNumber, Mono.just(new Operation(cardValue, "Automatic wallet withdraw")))
                .flatMap(response -> {
                    MessageKafka responseMessage = new MessageKafka();
                    responseMessage.setCorrelationId(correlationId);
                    try {
                        responseMessage.setInformation(response.getId());
                        responseMessage.setStatus(true);
                        String responseMessageJson = objectMapper.writeValueAsString(responseMessage);
                        System.out.println("Enviando respuesta a Kafka: " + responseMessageJson);
                        return Mono.fromCallable(() -> kafkaTemplate.send(RESPONSE_TOPIC_WITHDRAW, responseMessageJson));
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Error serializing MessageKafka", e));
                    }
                })
                .onErrorResume(e -> {
                    System.out.println("Error específico: " + e.getMessage());
                    // Crear un mensaje de respuesta de error específico
                    MessageKafka errorMessage = new MessageKafka();
                    errorMessage.setCorrelationId(correlationId);
                    errorMessage.setStatus(false);
                    errorMessage.setMessage("Card not found or insufficient balance");
                    try {
                        String errorMessageJson = objectMapper.writeValueAsString(errorMessage);
                        System.out.println("Enviando respuesta de error a Kafka: " + errorMessageJson);
                        return Mono.fromCallable(() -> kafkaTemplate.send(RESPONSE_TOPIC_WITHDRAW, errorMessageJson)); // Indicar que hubo un error
                    } catch (Exception ex) {
                        return Mono.error(new RuntimeException("Error serializing error message", ex));
                    }
                })
                .doOnSuccess(response -> System.out.println("Mensaje enviado a Kafka exitosamente."))
                .subscribe();
    }
}
