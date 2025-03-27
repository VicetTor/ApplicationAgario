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

public class Game {
    private QuadTree quadTree;
    private List<Entity> robots;
    private Player player;
    private double xMin = 0;
    private double yMin = 0;
    private double xMax;
    private double yMax;
    private final int ROBOT_NUMBER = 25;

    public Game(QuadTree quadTree, String name) {
        this.quadTree = quadTree;
        this.xMin = quadTree.getDimension().getxMin();
        this.yMin = quadTree.getDimension().getyMin();
        this.xMax = quadTree.getDimension().getxMax();
        this.yMax = quadTree.getDimension().getyMax();
        this.player = (Player) new PlayerFactory(name, xMax, yMax).launchFactory();;

        // Initialisation des IA
        this.robots = new ArrayList<>();
        createRandomRobots(ROBOT_NUMBER);
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

    public void createRandomRobots(int limite) {
        for (int nb = 0; nb < limite; nb++) {
            robots.add(new IAFactory(xMax, yMax, quadTree).launchFactory());
        }
    }

    public void updateWorld() {
        HashMap<Player, List<Entity>> playerEntities = new HashMap<>();
    }

    public void eatEntity(List<Entity> entities, MovableEntity movableEntity, GameController g) {
        List<Entity> entityToRemove = new ArrayList<>();

        for (Entity entity : entities) {
            double dx = movableEntity.getPosX() - entity.getPosX();
            double dy = movableEntity.getPosY() - entity.getPosY();
            double squareDistance = dx * dx + dy * dy;

            if (squareDistance <= movableEntity.getRadius() * (movableEntity.getRadius()*2)
                    && movableEntity.getMass() >= (entity.getMass() * 1.33)) {


                if(entity instanceof Player){
                    g.eatPlayer();
                    break;
                }

                //TODO BUG ANIMATION AVEC LES PELLETS DES ROBOTS PAS A COTE DU JOUEUR
                if (entity instanceof Pellet) {

                    if (movableEntity instanceof Player) {

                        g.animatePelletConsumption(entity, movableEntity);
                        if(entity instanceof SpeedIncreasePellet || entity instanceof SpeedDecreasePellet){
                            ((SpecialPellet) entity).doSpecialPelletAction(g, movableEntity);
                        }

                    }
                    if(entity instanceof InvisiblePellet){
                        ((InvisiblePellet) entity).doSpecialPelletAction(g, movableEntity);
                    }

                }
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
            g.removeEntityFromHashMap(entity);
        }
    }
}