package com.example.agario.models;

import com.example.agario.models.factory.IAFactory;
import com.example.agario.models.factory.PelletFactory;
import com.example.agario.utils.QuadTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Game {
    private QuadTree quadTree;
    private List<Entity> robots;
    private Player player;
    private double xMin = 0;
    private double yMin = 0;
    private double xMax;
    private double yMax;

    private final int ROBOT_NUMBER = 25;

    public Game(QuadTree quadTree, Player player) {
        this.quadTree = quadTree;
        this.xMin = quadTree.getDimension().getxMin();
        this.yMin = quadTree.getDimension().getyMin();
        this.xMax = quadTree.getDimension().getxMax();
        this.yMax = quadTree.getDimension().getyMax();
        this.player = player;
        quadTree.insertNode(player);

        // Initialisation des IA
        this.robots = new ArrayList<>();
        for(int i = 0; i < ROBOT_NUMBER; i++){
            robots.add(new IAFactory(xMax, yMax, quadTree).launchFactory());
        }
    }

    public Player getPlayer() {
        return player;
    }

    public List<Entity> getRobots() {
        return robots;
    }

    public QuadTree getQuadTree() {
        return quadTree;
    }

    public void createRandomPellets(int limite) {
        for (int nb = 0; nb < limite; nb++) {
            Random rand = new Random();
            quadTree.insertNode(new PelletFactory(rand.nextDouble(xMax), rand.nextDouble(yMax)).launchFactory());
        }
    }

    public void updateWorld() {
        HashMap<Player, List<Entity>> playerEntities = new HashMap<>();
    }

    public void eatEntity(List<Entity> entities, MovableEntity movableEntity) {
        List<Entity> entityToRemove = new ArrayList<>();

        for (Entity entity : entities) {
            double dx = movableEntity.getPosX() - entity.getPosX();
            double dy = movableEntity.getPosY() - entity.getPosY();
            double squareDistance = dx * dx + dy * dy;

            if (squareDistance <= movableEntity.getRadius() * movableEntity.getRadius()
                && movableEntity.getMass() >= (entity.getMass() * 1.33)) {
                // Ajouter à la liste de suppression
                entityToRemove.add(entity);

                // Augmenter la masse de l'entité
                double newMass = movableEntity.getMass() + entity.getMass();
                movableEntity.setMass(newMass);
            }
        }

        // Supprimer les pellets mangés
        for (Entity entity : entityToRemove) {
            quadTree.removeNode(entity, quadTree);
            if (entity instanceof IA)
                robots.remove(entity);
            entities.remove(entity);
        }
    }
}