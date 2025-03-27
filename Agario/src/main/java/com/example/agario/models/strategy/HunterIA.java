package com.example.agario.models.strategy;

import com.example.agario.models.Entity;
import com.example.agario.models.IA;
import com.example.agario.models.Player;
import com.example.agario.models.utils.Dimension;
import com.example.agario.models.utils.QuadTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class HunterIA implements Strategy{

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
    private final int HUNTING_AREA = 300;
    private QuadTree quadTree;
    private Player player;

    public HunterIA(IA robot, QuadTree quadTree, Player player){
        this.robot = robot;
        this.quadTree = quadTree;
        this.player = player;
        this.dimension = quadTree.getDimension();
        lastDirectionChangeTime = System.currentTimeMillis();
    }

    @Override
    public List<Double> behaviorIA() {
        this.dimension = new Dimension(
                robot.getPosX() - HUNTING_AREA,
                robot.getPosY() - HUNTING_AREA,
                robot.getPosX() + HUNTING_AREA,
                robot.getPosY() + HUNTING_AREA
        );

        if(dimension.inRange(player.getPosX(), player.getPosY()) && (robot.getMass()/player.getMass()) > 1.33){
            return List.of(player.getPosX(), player.getPosY());
        }
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

            x = (movingRight)? Math.min(robot.getPosX()+ 300, dimension.getxMax()) : rand.nextDouble(dimension.getxMax());
            y = (movingDown)? Math.min(robot.getPosY() + 300, dimension.getyMax()) : rand.nextDouble(dimension.getyMax());
            lastDirectionChangeTime = System.currentTimeMillis();
        }
        return List.of(x, y);
    }
}
