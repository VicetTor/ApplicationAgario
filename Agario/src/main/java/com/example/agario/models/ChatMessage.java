package com.example.agario.models;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private final String sender;
    private final String message;

    public ChatMessage(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }
}