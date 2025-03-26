package com.example.agario.models.strategy;

import com.example.agario.models.Entity;
import com.example.agario.models.Pellet;
import com.example.agario.utils.Dimension;
import com.example.agario.utils.QuadTree;

import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GluttonIA implements Strategy{

    private final int EATING_AREA_DIMENSION = 500;
    private Dimension dimension;
    private double x;
    private double y;
    private QuadTree quadTree;
    private double HEIGHT;
    private double WIDTH;

    private long lastDirectionChangeTime;
    private final int maxTime = 2500;
    private final int minTime = 300;

    private boolean movingRight = true;
    private boolean movingDown = true;

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
        dimension = new Dimension(
                ((x - EATING_AREA_DIMENSION) < 0)? 0:(x - EATING_AREA_DIMENSION),
                ((y - EATING_AREA_DIMENSION) < 0)? 0:(y - EATING_AREA_DIMENSION),
                ((x + EATING_AREA_DIMENSION) < WIDTH)? WIDTH:(x + EATING_AREA_DIMENSION) ,
                ((y + EATING_AREA_DIMENSION) < HEIGHT)? HEIGHT:(y + EATING_AREA_DIMENSION));
    }

    @Override
    public List<Double> behaviorIA() {
        ArrayList<Double> direction = new ArrayList<>();

        ArrayList<Entity> pelletsList = new ArrayList<>();
        QuadTree.DFSChunk(quadTree, dimension, pelletsList);// Collect all the pellets in the dimension area
        for(Entity pellet: pelletsList){
            if(pellet instanceof Pellet) {
                if (dimension.inRange(pellet.getPosX(), pellet.getPosY())) { // goes into the coordinates of the pellets
                    direction.add(pellet.getPosX());
                    direction.add(pellet.getPosY());
                    recalculateDimensionArea(pellet.getPosX(), pellet.getPosY());
                    return direction;
                }
            }
        }
        // if the IA doesn't find any pellet, it goes into random direction into the dimension area
        return randomDirection();
    }

    private List<Double> randomDirection(){
        int randomTime = new Random().nextInt(maxTime - minTime + 1) + minTime;
        if((System.currentTimeMillis()-lastDirectionChangeTime) > randomTime){
            if (new Random().nextInt(100) < 50) { movingRight = !movingRight;}
            if (new Random().nextInt(100) < 50) { movingDown = !movingDown;}

            x = (movingRight)? + 100 : new Random().nextDouble(WIDTH);
            y = (movingDown)? + 100 : new Random().nextDouble(HEIGHT);
            lastDirectionChangeTime = System.currentTimeMillis();
        }
        return List.of(x, y);
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

