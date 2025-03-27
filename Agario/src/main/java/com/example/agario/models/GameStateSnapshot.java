package com.example.agario.models;

import java.io.Serializable;
import java.util.List;

public class GameStateSnapshot implements Serializable {


    public List<Player> getPlayers() {
        return players;
    }

    public List<Entity> getPellets() {
        return pellets;
    }

    private final List<Player> players;
    private final List<Entity> pellets;


    public GameStateSnapshot(GameState game) {
        this.players = game.getPlayers();
        this.pellets = game.getQuadTree().getAllPellets();
    }


}