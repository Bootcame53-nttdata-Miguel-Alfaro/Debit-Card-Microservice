package com.nttdata.bank.debit_card.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageKafka {
    private String information;
    private String message;
    private String phoneNumber;
    private String correlationId;
    private Double value;
    private Boolean status;

    @JsonCreator
    public MessageKafka(
            @JsonProperty("information") String information,
            @JsonProperty("message") String message,
            @JsonProperty("phoneNumber") String phoneNumber,
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("status") Boolean status,
            @JsonProperty("value") Double value) {
        this.information = information;
        this.message = message;
        this.phoneNumber = phoneNumber;
        this.correlationId = correlationId;
        this.value = value;
        this.status = status;
    }
    public MessageKafka(){}
}