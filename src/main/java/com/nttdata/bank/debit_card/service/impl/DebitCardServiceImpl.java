package com.nttdata.bank.debit_card.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.bank.debit_card.domain.Account;
import com.nttdata.bank.debit_card.domain.DebitCard;
import com.nttdata.bank.debit_card.domain.MessageKafka;
import com.nttdata.bank.debit_card.repository.DebitCardRepository;
import com.nttdata.bank.debit_card.service.AccountService;
import com.nttdata.bank.debit_card.service.DebitCardService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
public class DebitCardServiceImpl implements DebitCardService {

    private final DebitCardRepository debitCardRepository;
    private final AccountService accountService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String REQUEST_TOPIC = "card_validation_request";
    private static final String RESPONSE_TOPIC = "card_validation_response";

    public DebitCardServiceImpl(DebitCardRepository debitCardRepository, AccountService accountService,
                                KafkaTemplate<String, String> kafkaTemplate) {
        this.debitCardRepository = debitCardRepository;
        this.accountService = accountService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<DebitCard> save(Mono<DebitCard> debitCard) {
        return debitCard
                .flatMap(card ->
                        // Verificar que el primaryAccountId pertenezca al customerId
                        accountService.findAccountsByCustomerId(card.getCustomerId())
                                .filter(account -> account.getId().equals(card.getPrimaryAccountId()))
                                .hasElements()
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.just(card);
                                    } else {
                                        return Mono.error(new RuntimeException("Primary account ID does not belong to the customer"));
                                    }
                                })
                )
                .doOnNext(card -> {
                    card.setCreatedAt(new Date());
                    // Añadir el primaryAccountId a la lista de linkedAccountIds
                    if (!card.getLinkedAccountIds().contains(card.getPrimaryAccountId())) {
                        card.getLinkedAccountIds().add(card.getPrimaryAccountId());
                    }
                })
                .flatMap(debitCardRepository::save);
    }

    @Override
    public Mono<DebitCard> linkAccount(String cardId, String accountId) {
        return debitCardRepository.findById(cardId)
                .flatMap(debitCard -> {
                    // Validar si ya esta en registrado
                    if (debitCard.getLinkedAccountIds().contains(accountId)) {
                        return Mono.error(new RuntimeException("Account already linked"));
                    }
                    // Obtenemos el cliente
                    return accountService.findAccountsByCustomerId(debitCard.getCustomerId())
                            .filter(account -> account.getId().equals(accountId))
                            .next() // Obtenga la primera cuenta coincidente o Mono.empty() osea vacio si no se encuentra ninguna
                            .switchIfEmpty(Mono.error(new RuntimeException("Account not found or not associated with customer")))
                            .flatMap(account -> {
                                // Add accountId to linkedAccountIds and save
                                debitCard.getLinkedAccountIds().add(accountId);
                                return debitCardRepository.save(debitCard);
                            });
                });
    }

    @Override
    public Mono<DebitCard> setPrimaryAccount(String cardId, String primaryAccountId) {
        return debitCardRepository.findById(cardId)
                .flatMap(debitCard -> {
                    // Validamos si el PrimaryAccountId ya está en linkedAccountIds
                    if (!debitCard.getLinkedAccountIds().contains(primaryAccountId)) {
                        return Mono.error(new RuntimeException("Account is not linked to the debit card"));
                    }
                    // Comprovamos si ya es principal
                    if (debitCard.getPrimaryAccountId().equals(primaryAccountId)) {
                        return Mono.error(new RuntimeException("Account is already the primary account"));
                    }

                    // Actualizamos el ID de cuenta principal y reordenamos los ID de las cuentas vinculadas
                    debitCard.setPrimaryAccountId(primaryAccountId);
                    debitCard.getLinkedAccountIds().remove(primaryAccountId);
                    debitCard.getLinkedAccountIds().add(0, primaryAccountId);
                    return debitCardRepository.save(debitCard);
                });
    }

    @Override
    public Mono<DebitCard> findById(String cardId) {
        return debitCardRepository.findByCardNumber(cardId);
    }

    @Override
    public Mono<Void> delete(String cardId) {
        return debitCardRepository.findById(cardId)
                .flatMap(c -> debitCardRepository.deleteById(cardId));
    }

    @Override
    public Mono<Account> getBalance(String cardNumber) {
        return debitCardRepository.findByCardNumber(cardNumber)
                .switchIfEmpty(Mono.error(new RuntimeException("Debit card not found")))
                .flatMap(c -> accountService.findMainAccountBalance(c.getPrimaryAccountId()));
    }

    @KafkaListener(topics = REQUEST_TOPIC, groupId = "debit_card_service_group")
    public void listen(String messageJson) {
        System.out.println("Solicitud recibida de Kafka: " + messageJson);

        MessageKafka message;
        try {
            message = objectMapper.readValue(messageJson, MessageKafka.class);
        } catch (Exception e) {
            System.out.println("Fallo");
            throw new RuntimeException("Error deserializing MessageKafka", e);
        }

        String cardNumber = message.getInformation();
        String correlationId = message.getCorrelationId();

        System.out.println("Buscando tarjeta con número: " + cardNumber);

        debitCardRepository.findAllByCardNumber(cardNumber)
                .hasElements()
                .map(exists -> {
                    MessageKafka responseMessage = new MessageKafka();
                    responseMessage.setStatus(exists);
                    responseMessage.setCorrelationId(correlationId);
                    return responseMessage;
                })
                .flatMap(responseMessage -> {
                    try {
                        String responseMessageJson = objectMapper.writeValueAsString(responseMessage);
                        System.out.println("Enviando respuesta a Kafka: " + responseMessageJson);
                        return Mono.fromCallable(() -> kafkaTemplate.send(RESPONSE_TOPIC, responseMessageJson));
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Error serializing MessageKafka", e));
                    }
                })
                .doOnError(e -> System.out.println("Error durante el procesamiento: " + e.getMessage()))
                .doOnSuccess(response -> System.out.println("Mensaje enviado a Kafka exitosamente."))
                .subscribe();
    }
}