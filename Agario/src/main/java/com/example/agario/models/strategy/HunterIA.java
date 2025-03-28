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
    private final int HUNTING_AREA = 150;
    private QuadTree quadTree;
    private Player player;

    private List<Entity> entities;

    /**
     * constructor for HunterIA
     * @param robot
     * @param quadTree
     * @param player
     * @param entities
     */
    public HunterIA(IA robot, QuadTree quadTree, Player player, List<Entity> entities){
        this.robot = robot;
        this.quadTree = quadTree;
        this.player = player;
        this.dimension = quadTree.getDimension();
        this.entities = entities;
        lastDirectionChangeTime = System.currentTimeMillis();
    }

    /**
     * @return List<Double> of x and y coordinates to make AI move in the direction of the player
     */
    @Override
    public List<Double> behaviorIA() {
        this.dimension = new Dimension(
                robot.getPosX() - HUNTING_AREA,
                robot.getPosY() - HUNTING_AREA,
                robot.getPosX() + HUNTING_AREA,
                robot.getPosY() + HUNTING_AREA
        );
        List<Entity> result = new ArrayList<>(entities);
        result.add(player);
        result.remove(robot);

        result.sort((botA, botB)->{
            double distanceBotA = distanceCalculation(robot.getPosX(), robot.getPosY(), botA.getPosX(), botA.getPosY());
            double distanceBotB = distanceCalculation(robot.getPosX(), robot.getPosY(), botB.getPosX(), botB.getPosY());
            return Double.compare(distanceBotA, distanceBotB);
        });

        if(!result.isEmpty()){
            Entity closestTarget = result.get(0);
            if( robot.getMass() >= (closestTarget.getMass()*1.33) && dimension.inRange(closestTarget.getPosX(), closestTarget.getPosY()))
                return List.of(closestTarget.getPosX(), closestTarget.getPosY());
            else
                return eatingPelletDirection();
        }
        return randomDirection();
    }

    /**
     * @return List<Double> of x and y coordinates of the closest pellet in the hunting area
     */
    public List<Double> eatingPelletDirection(){
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

            x = (movingRight)? Math.min(robot.getPosX()+ 300, dimension.getxMax()) : rand.nextDouble(dimension.getxMax());
            y = (movingDown)? Math.min(robot.getPosY() + 300, dimension.getyMax()) : rand.nextDouble(dimension.getyMax());
            lastDirectionChangeTime = System.currentTimeMillis();
        }
        return List.of(x, y);
    }
}
