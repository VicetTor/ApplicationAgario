package com.example.agario.models;

import java.io.Serializable;

public class InitGameData implements Serializable {
    private int playerId;
    private double worldWidth;
    private double worldHeight;

    public InitGameData(int playerId, double worldWidth, double worldHeight) {
        this.playerId = playerId;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    // Getters
    public int getPlayerId() {
        return playerId;
    }

    public double getWorldWidth() {
        return worldWidth;
    }

    public double getWorldHeight() {
        return worldHeight;
    }
}