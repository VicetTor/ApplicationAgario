package com.example.agario.models;

import com.example.agario.models.utils.QuadTree;

import java.util.List;

public interface GameInterface {

    List<Player> getPlayers();
    List<Entity> getRobots();
    QuadTree getQuadTree();
    void updatePlayer(Player player);
    void addPlayer(Player player);

    Player getPlayer();

    void createRandomPellets(int i);

    void createRandomRobots(int limite);

    void setRobots(List<Entity> robots);
}
