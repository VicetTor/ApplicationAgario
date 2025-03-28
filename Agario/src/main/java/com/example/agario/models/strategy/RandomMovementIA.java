package com.example.agario.models.strategy;

import com.example.agario.models.utils.Dimension;
import com.example.agario.models.Entity;
import com.example.agario.models.IA;
import com.example.agario.models.utils.Dimension;
import com.example.agario.models.utils.QuadTree;

import java.util.List;
import java.util.Random;

public class RandomMovementIA implements Strategy{

    private double x;
    private double y;

    private IA robot;
    private Dimension dimension;
    private long lastDirectionChangeTime;
    private final int maxTime = 2500;
    private final int minTime = 300;

    private boolean movingRight = true;
    private boolean movingDown = true;

    private final Random rand = new Random();

    /**
     * constructor for RandomMovementIA
     * @param robot
     * @param dimension
     */
    public RandomMovementIA(IA robot, Dimension dimension){
        this.robot = robot;
        this.dimension = dimension;
        lastDirectionChangeTime = System.currentTimeMillis();
    }

    /**
     * @return List<Double> of random x and y coordinates to make AI move
     */
    @Override
    public List<Double> behaviorIA() {
        return randomDirection();
    }

    /**
     * @return List<Double> of random x and y coordinates to make AI move
     */
    private List<Double> randomDirection(){
        int randomTime = rand.nextInt(maxTime - minTime + 1) + minTime;
        if((System.currentTimeMillis()-lastDirectionChangeTime) > randomTime){
            if (rand.nextInt(100) < 25) { movingRight = !movingRight;}
            if (rand.nextInt(100) < 25) { movingDown = !movingDown;}

            x = (movingRight)? Math.min(robot.getPosX()+ 250, dimension.getxMax()) : rand.nextDouble(dimension.getxMax());
            y = (movingDown)? Math.min(robot.getPosY() + 250, dimension.getyMax()) : rand.nextDouble(dimension.getyMax());
            lastDirectionChangeTime = System.currentTimeMillis();
        }
        return List.of(x, y);
    }
}

