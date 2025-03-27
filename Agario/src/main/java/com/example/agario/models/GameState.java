package com.example.agario.models;

import com.example.agario.models.utils.QuadTree;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class GameState implements Serializable {

    private Collection<Player> players;

    // pellets DATA
    private QuadTree pellets;
    private List<IA> bots;

    private long timestamp;
    private double worldWidth;
    private double worldHeight;

    public GameState(){

    }

    public GameState(Collection<Player> players,
                     QuadTree pellets,
                     List<IA> bots,
                     double worldWidth,
                     double worldHeight) {
        this.players = players;
        this.pellets = pellets;
        this.bots = bots;
        this.timestamp = System.currentTimeMillis();
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }


    public Collection<Player> getPlayers() {
        return players;
    }

    public QuadTree getPellets() {
        return pellets;
    }

    public List<IA> getBots() {
        return bots;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // ... autres getters
}
