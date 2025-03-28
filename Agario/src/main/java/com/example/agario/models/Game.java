package com.example.agario.models;

import com.example.agario.client.controllers.GameController;
import com.example.agario.models.factory.IAFactory;
import com.example.agario.models.factory.PelletFactory;

import com.example.agario.models.utils.QuadTree;

import com.example.agario.models.factory.PlayerFactory;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Game implements GameInterface {

    private static Game instance;
    private QuadTree quadTree;
    private List<Entity> robots;
    private Player player;
    private List<Player> players;
    private double xMin = 0;
    private double yMin = 0;
    private double xMax;
    private double yMax;

    /**
     * getter for players
     * @return list of players
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * setter for players
     * @param players list of players to apply
     */
    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    /**
     * constructor for Game
     * @param quadTree game's QuadTree
     * @param name name of the player
     * @param robotNumber number of bots
     */
    public Game(QuadTree quadTree, String name, int robotNumber) {
        this.quadTree = quadTree;
        this.xMin = quadTree.getDimension().getxMin();
        this.yMin = quadTree.getDimension().getyMin();
        this.xMax = quadTree.getDimension().getxMax();
        this.yMax = quadTree.getDimension().getyMax();
        this.player = (Player) new PlayerFactory(name, xMax, yMax).launchFactory();;
        this.players = new ArrayList<>();
        // Initialisation des IA
        this.robots = new ArrayList<>();
        createRandomRobots(robotNumber);
    }

    /**
     * @return the player of the game
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the robots in the game
     */
    public List<Entity> getRobots() {
        return robots;
    }

    /**
     * @return the data structure quadTree of the game
     */
    public QuadTree getQuadTree() {
        return quadTree;
    }

    /**
     * create new pellets in different places
     *
     * @param limite number of new pellets
     */
    public void createRandomPellets(int limite) {
        for (int nb = 0; nb < limite; nb++) {
            Random rand = new Random();
            quadTree.insertNode(new PelletFactory(rand.nextDouble(xMax), rand.nextDouble(yMax)).launchFactory());
        }
    }

    /**
     * update the object player
     *
     * @param updatedPlayer player
     */
    public synchronized void updatePlayer(Player updatedPlayer) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(updatedPlayer.getName())) {
                players.set(i, updatedPlayer);
                break;
            }
        }
    }


    /**
     * add a player to the game
     *
     * @param player new player
     */
    public synchronized void addPlayer(Player player) {
        this.players.add(player);
    }

    /*public static synchronized Game getInstance(QuadTree quadTree, String name) {
        if (instance == null) {
            instance = new Game(quadTree, name);
        }
        return instance;
    }*/

    /**
     * create new robots in different places in the game
     *
     * @param limite number of new robots
     */
    public void createRandomRobots(int limite) {
        for (int nb = 0; nb < limite; nb++) {
            robots.add(new IAFactory(xMax, yMax, quadTree, player, robots).launchFactory());
        }
    }

    public void updateWorld() {
        HashMap<Player, List<Entity>> playerEntities = new HashMap<>();
    }

    /**
     *
     * getters and setters of the class
     *
     */
    public void setQuadTree(QuadTree quadTree) {
        this.quadTree = quadTree;
    }

    public void setRobots(List<Entity> robots) {
        this.robots = robots;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public double getxMin() {
        return xMin;
    }

    public void setxMin(double xMin) {
        this.xMin = xMin;
    }

    public double getyMin() {
        return yMin;
    }

    public void setyMin(double yMin) {
        this.yMin = yMin;
    }

    public double getxMax() {
        return xMax;
    }

    public void setxMax(double xMax) {
        this.xMax = xMax;
    }

    public double getyMax() {
        return yMax;
    }

    public void setyMax(double yMax) {
        this.yMax = yMax;
    }
}