package com.jacto.scheduler.payload.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageResponse {
    private String message;

    // Construtor padrão para deserialização JSON
    public MessageResponse() {
    }

    // Construtor com mensagem
    @JsonCreator
    public MessageResponse(@JsonProperty("message") String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
