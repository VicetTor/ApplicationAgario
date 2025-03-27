package com.example.agario.models.strategy;

import com.example.agario.models.utils.Dimension;

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
        int randomTime = new Random().nextInt(maxTime - minTime + 1) + minTime;
        if((System.currentTimeMillis()-lastDirectionChangeTime) > randomTime){
            if (new Random().nextInt(100) < 50) { movingRight = !movingRight;}
            if (new Random().nextInt(100) < 50) { movingDown = !movingDown;}

            x = (movingRight)? + 100 : new Random().nextDouble(dimension.getxMax());
            y = (movingDown)? + 100 : new Random().nextDouble(dimension.getyMax());
            lastDirectionChangeTime = System.currentTimeMillis();
        }
        return List.of(x, y);
    }
}

