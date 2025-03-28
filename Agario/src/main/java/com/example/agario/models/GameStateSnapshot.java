package com.example.agario.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameStateSnapshot implements Serializable {
    private static final long serialVersionUID = -8862885764214783136L;

    private final List<Player> players;


    private final List<Entity> pellets;
    private final boolean initialSnapshot;

    public GameStateSnapshot(GameState game) {
        this(game, false);
    }

    public GameStateSnapshot(GameState game, boolean initialSnapshot) {
        this.players = game.getPlayers();
        this.pellets = game.getQuadTree().getAllPellets();
        this.initialSnapshot = initialSnapshot;

    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Entity> getPellets() {
        return pellets;
    }



    public boolean isInitialSnapshot() {
        return initialSnapshot;
    }
}