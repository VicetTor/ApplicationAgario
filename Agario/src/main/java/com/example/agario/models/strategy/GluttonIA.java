package com.example.agario.models.strategy;

import com.example.agario.models.Entity;
import com.example.agario.models.IA;
import com.example.agario.models.Pellet;
import com.example.agario.utils.Dimension;
import com.example.agario.utils.QuadTree;

import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GluttonIA implements Strategy{

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

    private final int EATING_AREA = 150;

    public GluttonIA(IA robot, QuadTree quadTree){
        this.robot = robot;
        this.dimension = quadTree.getDimension();
        lastDirectionChangeTime = System.currentTimeMillis();
    }


    @Override
    public List<Double> behaviorIA() {
        /*
        this.dimension = new Dimension(
                robot.getPosX()
        );
        if(pellet is in dimension){
            manger(pellet)
        }
        else{
            randomDirection();
        }*/

        return randomDirection();
    }

    /**
     * @return List<Double> of random coordinate to make move the AI
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

