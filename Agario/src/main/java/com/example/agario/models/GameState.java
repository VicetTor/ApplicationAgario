package com.example.agario.models;

import com.example.agario.client.controllers.GameController;
import com.example.agario.models.factory.IAFactory;
import com.example.agario.models.factory.PelletFactory;

import com.example.agario.models.utils.QuadTree;

import com.example.agario.models.factory.PlayerFactory;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class GameState implements Serializable {
    private static final long serialVersionUID = -8862885764214783136L;
    private static GameState instance;
    private final QuadTree quadTree;

    private Player localPlayer;
    private final List<Entity> robots;
    private final List<Player> players;
    private final double xMin;
    private final double yMin;
    private final double xMax;
    private final double yMax;
    private final int ROBOT_NUMBER = 25;

    // Constructeur privé
    private GameState(QuadTree quadTree, String name) {
        this.quadTree = quadTree;
        this.xMin = quadTree.getDimension().getxMin();
        this.yMin = quadTree.getDimension().getyMin();
        this.xMax = quadTree.getDimension().getxMax();
        this.yMax = quadTree.getDimension().getyMax();
        this.players = new ArrayList<>();
        this.robots = new ArrayList<>();

        this.localPlayer = new Player(0, 0, name);
        this.players.add(localPlayer);
        createRandomRobots(ROBOT_NUMBER);
    }

    // Méthode singleton
    public static synchronized GameState getInstance(QuadTree quadTree, String name) {
        if (instance == null) {
            instance = new GameState(quadTree, name);
        }
        return instance;
    }

    // Méthodes synchronisées pour la gestion des joueurs
    public synchronized List<Player> getPlayers() {
        return new ArrayList<>(players);  // Retourne une copie
    }

    public synchronized void addPlayer(Player player) {
        if (players.stream().noneMatch(p -> p.getName().equals(player.getName()))) {
            players.add(player);
        }
    }

    public Player getPlayer() {
        return localPlayer;
    }

    public synchronized void removePlayer(String playerName) {
        players.removeIf(p -> p.getName().equals(playerName));
    }

    public synchronized void updatePlayer(Player updatedPlayer) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(updatedPlayer.getName())) {
                players.set(i, updatedPlayer);
                break;
            }
        }
    }



    // Gestion des entités
    public synchronized void createRandomPellets(int limit) {
        Random rand = new Random();
        for (int nb = 0; nb < limit; nb++) {
            quadTree.insertNode(new PelletFactory(rand.nextDouble(xMax), rand.nextDouble(yMax)).launchFactory());
        }
    }

    public synchronized void createRandomRobots(int limit) {
        for (int nb = 0; nb < limit; nb++) {
            robots.add(new IAFactory(xMax, yMax, quadTree).launchFactory());
        }
    }


    public void setRobots(List<Entity> robots) {
        this.robots.clear();
        this.robots.addAll(robots);

    }

    // Getters
    public synchronized List<Entity> getRobots() {
        return new ArrayList<>(robots);  // Retourne une copie
    }

    public QuadTree getQuadTree() {
        return quadTree;
    }

    public double getxMin() { return xMin; }
    public double getyMin() { return yMin; }
    public double getxMax() { return xMax; }
    public double getyMax() { return yMax; }
    public int getROBOT_NUMBER() { return ROBOT_NUMBER; }

    // Méthode pour obtenir toutes les entités (pour le broadcast)
    public synchronized List<Entity> getAllEntities() {
        List<Entity> entities = new ArrayList<>();
        entities.addAll(players);
        entities.addAll(robots);
        entities.addAll(quadTree.getAllPellets());  // À implémenter dans QuadTree
        return entities;
    }
}