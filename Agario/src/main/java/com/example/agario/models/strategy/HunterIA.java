package com.example.agario.models.strategy;

import com.example.agario.models.Entity;
import com.example.agario.models.Player;
import com.example.agario.models.utils.Dimension;
import com.example.agario.models.utils.QuadTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HunterIA implements Strategy{

    //TODO all the classes because copy/paste of GluttonIA

    private final int EATING_AREA_DIMENSION = 500;
    private Dimension dimension;
    private double x;
    private double y;
    private QuadTree quadTree;

    private double HEIGHT;
    private double WIDTH;

    public HunterIA(double x, double y, QuadTree quadTree){
        this.x = x;
        this.y = y;
        this.quadTree = quadTree;
        WIDTH = quadTree.getDimension().getxMax();
        HEIGHT = quadTree.getDimension().getyMax();
        double xMax = x + EATING_AREA_DIMENSION;
        double yMax = y + EATING_AREA_DIMENSION;
        double xMin = x - EATING_AREA_DIMENSION;
        double yMin = y - EATING_AREA_DIMENSION;
        dimension = new Dimension(
                ((x - EATING_AREA_DIMENSION) < 0)? 0:(x - EATING_AREA_DIMENSION),
                ((y - EATING_AREA_DIMENSION) < 0)? 0:(y - EATING_AREA_DIMENSION),
                ((x + EATING_AREA_DIMENSION) < WIDTH)? WIDTH:(x + EATING_AREA_DIMENSION) ,
                ((y + EATING_AREA_DIMENSION) < HEIGHT)? HEIGHT:(y + EATING_AREA_DIMENSION));

    }
    @Override
    public List<Double> behaviorIA() {  //PROBLEM BECAUSE DOESNT FIND PLAYER
        ArrayList<Double> direction = new ArrayList<>();

        ArrayList<Entity> objectList = new ArrayList<>();
        QuadTree.DFSChunk(quadTree, dimension, objectList);// Collect all the pellets in the dimension area
        for(Entity object: objectList){
            if(object instanceof Player) {
                if (dimension.inRange(object.getPosX(), object.getPosY())) { // goes into the coordinates of the pellets
                    direction.add(object.getPosX());
                    direction.add(object.getPosY());
                    recalculateDimensionArea(object.getPosX(), object.getPosY());
                    return direction;
                }
            }
        }
        // if the IA doesn't find any pellet, it goes into random direction into the dimension area
        return randomDirection();
    }

    private List<Double> randomDirection(){
        ArrayList<Double> direction = new ArrayList<>();
        Random rand = new Random();

        double maxDistance = 100;
        double newX = x + (rand.nextDouble() * 2 -1) * maxDistance;
        double newY = y + (rand.nextDouble() * 2 -1) * maxDistance;

        newX = Math.max(dimension.getxMin(), Math.min(dimension.getxMax(), newX));
        newY = Math.max(dimension.getyMin(), Math.min(dimension.getyMax(), newY));
        direction.add(newX);
        direction.add(newY);
        recalculateDimensionArea(newX,newY);

        return direction;
    }

    private void recalculateDimensionArea(double x, double y){
        this.x = x;
        this.y = y;
        double xMax = x + EATING_AREA_DIMENSION;
        double yMax = y + EATING_AREA_DIMENSION;
        double xMin = x - EATING_AREA_DIMENSION;
        double yMin = y - EATING_AREA_DIMENSION;
        dimension.setxMax((xMax > WIDTH)? WIDTH : xMax);
        dimension.setyMax((yMax > HEIGHT)? HEIGHT : yMax);
        dimension.setxMin((xMin < 0)? 0 : xMin);
        dimension.setyMin((yMin < 0)? 0 : yMin);
    }
}
