package com.example.agario.models.strategy;

import com.example.agario.models.Entity;
import com.example.agario.utils.Dimension;
import com.example.agario.utils.QuadTree;

import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GluttonIA implements Strategy{

    private final int EATING_AREA_DIMENSION = 200;
    private Dimension dimension;
    private double x;
    private double y;
    private QuadTree quadTree;

    private double HEIGHT;
    private double WIDTH;

    public GluttonIA(double x, double y, QuadTree quadTree){
        this.x = x;
        this.y = y;
        this.quadTree = quadTree;
        WIDTH = quadTree.getDimension().getxMax();
        HEIGHT = quadTree.getDimension().getyMax();
        double xMax = x + EATING_AREA_DIMENSION;
        double yMax = y + EATING_AREA_DIMENSION;
        double xMin = x - EATING_AREA_DIMENSION;
        double yMin = y - EATING_AREA_DIMENSION;
        System.out.println("############################## XMin="+xMin+" yMin="+yMin+"Xmax ="+xMax+" YMax="+yMax);
        dimension = new Dimension(
                ((x - EATING_AREA_DIMENSION) < 0)? 0:(x - EATING_AREA_DIMENSION),
                ((y - EATING_AREA_DIMENSION) < 0)? 0:(y - EATING_AREA_DIMENSION),
                ((x + EATING_AREA_DIMENSION) < WIDTH)? WIDTH:(x + EATING_AREA_DIMENSION) ,
                ((y + EATING_AREA_DIMENSION) < HEIGHT)? HEIGHT:(y + EATING_AREA_DIMENSION));

    }
    @Override
    public List<Double> behaviorIA() {
        System.out.println("Emplacement IA x="+x+" y="+y);
        ArrayList<Double> direction = new ArrayList<>();

        ArrayList<Entity> pelletsList = new ArrayList<>();
        QuadTree.DFSChunk(quadTree, dimension, pelletsList);// Collect all the pellets in the dimension area
        for(Entity pellet: pelletsList){
            if(dimension.inRange(pellet.getPosX(), pellet.getPosY())){ // goes into the coordinates og the pellets
                direction.add(pellet.getPosX());
                direction.add(pellet.getPosY());
                recalculateDimensionArea(pellet.getPosX(),pellet.getPosY());
                return direction;
            }
        }
        // if the IA doesn't find any pellet, it goes into random direction into the dimension area
        double newX = new Random().nextDouble(dimension.getxMax()- dimension.getxMin() + 1) + dimension.getxMin();
        double newY = new Random().nextDouble(dimension.getyMax()- dimension.getyMin() + 1) + dimension.getyMin();
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

