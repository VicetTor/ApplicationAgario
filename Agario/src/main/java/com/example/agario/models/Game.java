package com.example.agario.models;

import com.example.agario.utils.QuadTree;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Game {
    private QuadTree quadTree;
    private double xMin = 0;
    private double yMin = 0;
    private double xMax;
    private double yMax;


    public Game(QuadTree quadTree){
        this.quadTree = quadTree;
        this.xMin = quadTree.getDimension().getxMin();
        this.yMin = quadTree.getDimension().getyMin();
        this.xMax = quadTree.getDimension().getxMax();
        this.yMax = quadTree.getDimension().getyMax();
    }

    public QuadTree getQuadTree(){
        return quadTree;
    }

    public void createRandomPellets(){
        for (int nb = 0; nb < 1000; nb++){
            Random rand = new Random();
            quadTree.insertNode(new PelletFactory(rand.nextDouble(xMax), rand.nextDouble(yMax)).launchFactory());
            System.out.println(nb);
        }
    }

    public void updateWorld(){
        HashMap<Player, List<Entity>> playerEntities = new HashMap<Player, List<Entity>>();
    }

    public void eatPellet(List<Entity> liste, Player player){
        for(Entity pellet : liste){
            if(pellet instanceof Pellet){
                double dx = (player.getPosX() - pellet.getPosX());
                double dy = (player.getPosY() - pellet.getPosY());
                double squareDistance = dx*dx + dy*dy;
                if(squareDistance <= player.getRadius()*player.getRadius()){
                    quadTree.removeNode(pellet, quadTree);
                }
            }
        }
    }
}
