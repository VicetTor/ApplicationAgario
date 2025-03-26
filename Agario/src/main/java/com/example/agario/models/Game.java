package com.example.agario.models;

import com.example.agario.models.factory.IAFactory;
import com.example.agario.models.factory.PelletFactory;
import com.example.agario.utils.Camera;
import com.example.agario.utils.QuadTree;
import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Game {
    private QuadTree quadTree;

    private List<Entity> robots;

    private Player player ;
    private double xMin = 0;
    private double yMin = 0;
    private double xMax;
    private double yMax;

    public Game(QuadTree quadTree, Player player){
        this.quadTree = quadTree;
        this.xMin = quadTree.getDimension().getxMin();
        this.yMin = quadTree.getDimension().getyMin();
        this.xMax = quadTree.getDimension().getxMax();
        this.yMax = quadTree.getDimension().getyMax();
        this.player = player;

        //initialisation des IA
        this.robots = new ArrayList<>();
        robots.add(new IAFactory(xMax,yMax,quadTree).launchFactory());
    }

    public Player getPlayer() {
        return player;
    }

    public List<Entity> getRobots() {
        return robots;
    }

    public QuadTree getQuadTree(){
        return quadTree;
    }

    public void createRandomPellets(int limite){
        for (int nb = 0; nb < limite; nb++){
            Random rand = new Random();
            quadTree.insertNode(new PelletFactory(rand.nextDouble(xMax), rand.nextDouble(yMax)).launchFactory());
            //System.out.println(nb);
        }
    }

    public void eatPellet(List<Entity> liste, MovableEntity movableEntity) {
        for (Entity pellet : liste) {
            if(pellet instanceof Pellet){
                double dx = (movableEntity.getPosX() - pellet.getPosX());
                double dy = (movableEntity.getPosY() - pellet.getPosY());
                double squareDistance = dx * dx + dy * dy;
                if (squareDistance <= movableEntity.getRadius() * player.getRadius()) {
                    quadTree.removeNode(pellet, quadTree);
                    double newMass = player.getMass() + 1;
                    player.setMass(newMass);
                }
            }
        }
    }
}
