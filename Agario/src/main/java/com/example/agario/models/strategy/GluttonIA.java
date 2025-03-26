package com.example.agario.models.strategy;

import com.example.agario.models.Entity;
import com.example.agario.utils.Dimension;
import com.example.agario.utils.QuadTree;

import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GluttonIA implements Strategy{

    private final int EATING_AREA_DIMENSION = 50;
    private Dimension dimension;
    private double x;
    private double y;
    private QuadTree quadTree;

    public GluttonIA(double x, double y, QuadTree quadTree){
        this.x = x;
        this.y = y;
        dimension = new Dimension(x - EATING_AREA_DIMENSION,y - EATING_AREA_DIMENSION,x + EATING_AREA_DIMENSION,y + EATING_AREA_DIMENSION);
        this.quadTree = quadTree;
    }
    @Override
    public List<Double> behaviorIA() {
        ArrayList<Double> direction = new ArrayList<>();

        ArrayList<Entity> pelletsList = new ArrayList<>();
        QuadTree.DFSChunk(quadTree, dimension, pelletsList);// Collect all the pellets in the dimension area
        for(Entity pellet: pelletsList){
            if(dimension.inRange(pellet.getPosX(), pellet.getPosY())){ // goes into the coordinates og the pellets
                direction.add(pellet.getPosX());
                direction.add(pellet.getPosY());
                return direction;
            }
        }
        // if the IA doesn't find any pellet, it goes into random direction into the dimension area
        direction.add(new Random().nextDouble(dimension.getxMax()- dimension.getxMin() + 1) + dimension.getxMin());
        direction.add(new Random().nextDouble(dimension.getyMax()- dimension.getyMin() + 1) + dimension.getyMin());
        return direction;
    }
}

