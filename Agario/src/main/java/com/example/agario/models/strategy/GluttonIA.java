package com.example.agario.models.strategy;

import com.example.agario.models.Entity;
import com.example.agario.models.IA;
import com.example.agario.models.Pellet;
import com.example.agario.models.utils.Dimension;
import com.example.agario.models.utils.QuadTree;

import java.util.ArrayList;
import java.util.Collections;
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
    private final int EATING_AREA = 200;
    private QuadTree quadTree;

    private Entity target;

    public GluttonIA(IA robot, QuadTree quadTree){
        this.robot = robot;
        this.quadTree = quadTree;
        this.dimension = quadTree.getDimension();
        lastDirectionChangeTime = System.currentTimeMillis();
    }


    /**
     * @return List<Double> of x and y coordinates of the closest pellet in the eating area
     */
    @Override
    public List<Double> behaviorIA() {
        this.dimension = new Dimension(
                robot.getPosX() - EATING_AREA,
                robot.getPosY() - EATING_AREA,
                robot.getPosX() + EATING_AREA,
                robot.getPosY() + EATING_AREA
            );

        List<Entity> result = new ArrayList<>();
        QuadTree.DFSChunk(quadTree,this.dimension,result);

        result.sort((pelletA, pelletB)->{
            double distancePelletA = distanceCalculation(robot.getPosX(),robot.getPosY(), pelletA.getPosX(), pelletA.getPosY());
            double distancePelletB = distanceCalculation(robot.getPosX(),robot.getPosY(), pelletB.getPosX(), pelletB.getPosY());
            return Double.compare(distancePelletA, distancePelletB);
        });

        if(!result.isEmpty()){
            Entity closestPellet = result.get(0);
            return List.of(closestPellet.getPosX(), closestPellet.getPosY());
        }

        return randomDirection();
    }

    /**
     * @return euclidian distance of two points
     */
    private double distanceCalculation(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
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

