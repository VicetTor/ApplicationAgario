package com.example.agario.models;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private String sender;
    private String message;

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