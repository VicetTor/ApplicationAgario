package com.example.agario.models.strategy;

import com.example.agario.models.Entity;
import com.example.agario.utils.Dimension;
import com.example.agario.utils.QuadTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomMovementIA implements Strategy{

    private double x;
    private double y;
    private Dimension dimension;
    private long lastDirectionChangeTime;
    private final int maxTime = 2500;
    private final int minTime = 300;

    private boolean movingRight = true;
    private boolean movingDown = true;

    public RandomMovementIA(double x, double y, Dimension dimension){
        this.x = x;
        this.y = y;
        this.dimension = dimension;
        lastDirectionChangeTime = System.currentTimeMillis();
    }

    @Override
    public List<Double> behaviorIA() {
        return randomDirection();
    }

    private List<Double> randomDirection(){
        Random rand = new Random();
        int randomTime = rand.nextInt(maxTime - minTime + 1) + minTime;
        if((System.currentTimeMillis()-lastDirectionChangeTime) > randomTime){
            if (rand.nextInt(100) < 50) { movingRight = !movingRight;}
            if (rand.nextInt(100) < 50) { movingDown = !movingDown;}

            x = (movingRight)? + 100 : rand.nextDouble(dimension.getxMax());
            y = (movingDown)? + 100 : rand.nextDouble(dimension.getyMax());
            lastDirectionChangeTime = System.currentTimeMillis();
        }
        return List.of(x, y);
    }
}

