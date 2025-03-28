package com.example.agario.models;

import java.io.Serializable;

/**
 * Object contains in the chat of the online game
 */
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private String sender;
    private String message;

    /**
     * Constructor
     *
     * @param sender sender of the message
     * @param message text of the message
     */
    public ChatMessage(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    /**
     * @return the sender of the message
     */
    public String getSender() {
        return sender;
    }

    /**
     * @return the text of the message
     */
    public String getMessage() {
        return message;
    }
}