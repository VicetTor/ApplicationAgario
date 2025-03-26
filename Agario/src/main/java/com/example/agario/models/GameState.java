package com.example.agario.models;

import java.io.Serializable;
import java.util.List;

public class GameState implements Serializable {
    private List<Player> players;
    private List<Entity> pellets;

    // Getters et setters
    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Entity> getPellets() {
        return pellets;
    }

    public void setPellets(List<Entity> pellets) {
        this.pellets = pellets;
    }
}