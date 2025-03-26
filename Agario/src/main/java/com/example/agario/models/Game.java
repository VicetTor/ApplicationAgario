package com.example.agario.models;

import com.example.agario.models.factory.IAFactory;
import com.example.agario.models.factory.PelletFactory;
import com.example.agario.utils.Camera;
import com.example.agario.utils.QuadTree;
import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.*;
import java.security.Key;

public class Game {
    private QuadTree quadTree;

    private List<Entity> robots;

    private Player player ;
    private double xMin = 0;
    private double yMin = 0;
    private double xMax;
    private double yMax;

    public Game(QuadTree quadTree, Player player){
        this.quadTree = quadTree;
        this.xMin = quadTree.getDimension().getxMin();
        this.yMin = quadTree.getDimension().getyMin();
        this.xMax = quadTree.getDimension().getxMax();
        this.yMax = quadTree.getDimension().getyMax();
        this.player = player;
        quadTree.insertNode(player);

        //initialisation des IA
        this.robots = new ArrayList<>();
        robots.add(new IAFactory(xMax,yMax,quadTree).launchFactory());
        robots.add(new IAFactory(xMax,yMax,quadTree).launchFactory());
        robots.add(new IAFactory(xMax,yMax,quadTree).launchFactory());
        for(Entity entity : robots){
            quadTree.insertNode(entity);
        }
    }

    public Player getPlayer() {
        return player;
    }

    public List<Entity> getRobots() {
        return robots;
    }

    public QuadTree getQuadTree(){
        return quadTree;
    }


    public void createRandomPellets(int limite){
        for (int nb = 0; nb < limite; nb++){
            Random rand = new Random();
            quadTree.insertNode(new PelletFactory(rand.nextDouble(xMax), rand.nextDouble(yMax)).launchFactory());
            //System.out.println(nb);
        }
    }

    public void updateWorld(){
        HashMap<Player, List<Entity>> playerEntities = new HashMap<Player, List<Entity>>();
    }

    public void eatPellet(List<Entity> liste, MovableEntity movableEntity,Circle playerCircle, Map<Entity,Circle> mapEntities){
        for(Entity pellet : liste){
            double dx = (movableEntity.getPosX() - pellet.getPosX());
            double dy = (movableEntity.getPosY() - pellet.getPosY());
            double squareDistance = dx*dx + dy*dy;
            if(squareDistance <= movableEntity.getRadius()*player.getRadius() * 2){

                Circle circlePullet = mapEntities.get(pellet);
                quadTree.removeNode(pellet, quadTree);
                double currentRadius = movableEntity.getRadius();

                double newMass = player.getMass() + 1;
                player.setMass(newMass);

                TranslateTransition transition = new TranslateTransition();
                transition.setNode(circlePullet);
                transition.setDuration(Duration.millis(10));
                transition.setToX(player.getPosX() - circlePullet.getCenterX());
                transition.setToY(player.getPosY() - circlePullet.getCenterY());
                transition.setAutoReverse(true);
                transition.setInterpolator(Interpolator.EASE_OUT);
                transition.play();

                Timeline growthTimeline = new Timeline(
                        new KeyFrame(Duration.ZERO, e -> {
                            playerCircle.radiusProperty().unbind();
                            playerCircle.setRadius(currentRadius);
                        }),
                        new KeyFrame(Duration.millis(10), e -> {
                            playerCircle.radiusProperty().unbind();
                            playerCircle.setRadius(this.player.getRadius());
                        })
                );

                growthTimeline.setCycleCount(1);
                growthTimeline.play();

            }
        }
    }
}
