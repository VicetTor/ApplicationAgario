package com.example.agario.models.strategy;

import com.example.agario.models.Entity;
import com.example.agario.models.IA;
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
    private double xMax;
    private double yMax;
    private double xMin;
    private double yMin;
    private QuadTree quadTree;

    private double HEIGHT;
    private double WIDTH;

    private long lastDirectionChangeTime;
    private final int maxTime = 2500;
    private final int minTime = 300;

    private final Random rand = new Random();

    private boolean movingRight = true;
    private boolean movingDown = true;
    private IA robot;

    public HunterIA(IA robot, QuadTree quadTree){
        this.robot = robot;
        this.quadTree = quadTree;
        WIDTH = quadTree.getDimension().getxMax();
        HEIGHT = quadTree.getDimension().getyMax();
        updateXMaxMinYMaxMin();
        lastDirectionChangeTime = System.currentTimeMillis();
    }

    private void updateXMaxMinYMaxMin(){
        xMax = x + EATING_AREA_DIMENSION;
        yMax = y + EATING_AREA_DIMENSION;
        xMin = x - EATING_AREA_DIMENSION;
        yMin = y - EATING_AREA_DIMENSION;
        dimension = new Dimension(
                (xMin < 0)? 0 : xMin,
                (yMin < 0)? 0 : yMin,
                (xMax < WIDTH)? WIDTH : xMax ,
                (yMax < HEIGHT)? HEIGHT : yMax);
    }

    @Override
    public List<Double> behaviorIA() {  //PROBLEM BECAUSE DOESNT FIND PLAYER
        ArrayList<Double> direction = new ArrayList<>();

        recalculateDimensionArea(x, y);

        ArrayList<Entity> objectList = new ArrayList<>();
        QuadTree.DFSChunk(quadTree, dimension, objectList);// Collect all the pellets in the dimension area
        for(Entity object: objectList){
            if(object instanceof Player) {
                if (dimension.inRange(object.getPosX(), object.getPosY())) { // goes into the coordinates of the pellets
                    direction.add(object.getPosX());
                    direction.add(object.getPosY());
                    return direction;
                }
            }
        }
        // if the IA doesn't find any player, it goes into random direction into the dimension area
        return randomDirection();
    }

    private List<Double> randomDirection(){
        int randomTime = rand.nextInt(maxTime - minTime + 1) + minTime;
        if((System.currentTimeMillis()-lastDirectionChangeTime) > randomTime){
            if (rand.nextInt(100) < 50) { movingRight = !movingRight;}
            if (rand.nextInt(100) < 50) { movingDown = !movingDown;}

            x = (movingRight)? + 100 : rand.nextDouble(WIDTH);
            y = (movingDown)? + 100 : rand.nextDouble(HEIGHT);
            lastDirectionChangeTime = System.currentTimeMillis();
        }
        return List.of(x, y);
    }

    private void recalculateDimensionArea(double x, double y){
        this.x = x;
        this.y = y;
        updateXMaxMinYMaxMin();
    }
}
