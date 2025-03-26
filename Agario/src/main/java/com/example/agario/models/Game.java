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

    public void eatPellet(List<Entity> liste, Entity entity){
        for(Entity pellet : liste){
            if(pellet instanceof Pellet){
                double dx = (entity.getPosX() - pellet.getPosX());
                double dy = (entity.getPosY() - pellet.getPosY());
                double squareDistance = dx*dx + dy*dy;
                if(squareDistance <= entity.getRadius()*entity.getRadius()){
                    quadTree.removeNode(pellet, quadTree);
                    double newRadius = entity.getRadius()+1;
                    entity.setRadius(newRadius);
                }
            }
        }
    }
}
