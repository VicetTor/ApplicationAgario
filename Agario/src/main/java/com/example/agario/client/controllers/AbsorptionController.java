package com.example.agario.client.controllers;

import com.example.agario.models.*;
import com.example.agario.models.specialPellet.InvisiblePellet;
import com.example.agario.models.specialPellet.SpeedDecreasePellet;
import com.example.agario.models.specialPellet.SpeedIncreasePellet;
import com.example.agario.models.utils.QuadTree;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AbsorptionController {

    private boolean isPlayerAlive;
    private Map<Entity, Circle> entitiesCircles;
    private double specialSpeed;


    public AbsorptionController(Map<Entity, Circle> entitiesCircles, double specialSpeed){
        this.isPlayerAlive = true;
        this.entitiesCircles = entitiesCircles;
        this.specialSpeed = specialSpeed;
    }


    public void eatEntity(List<Entity> entities, MovableEntity movableEntity, QuadTree quadTree, List<Entity> robots) {
        List<Entity> entityToRemove = new ArrayList<>();

        for (Entity entity : entities) {
            double dx = movableEntity.getPosX() - entity.getPosX();
            double dy = movableEntity.getPosY() - entity.getPosY();
            double squareDistance = dx * dx + dy * dy;

            if (squareDistance <= movableEntity.getRadius() * (movableEntity.getRadius()*2)
                    && movableEntity.getMass() >= (entity.getMass() * 1.33)) {
                if(entity instanceof Player){
                    this.eatPlayer();
                    break;
                }

                if (entity instanceof Pellet) {
                    if (movableEntity instanceof Player) {
                        this.animatePelletConsumption(entity, movableEntity);
                        if(entity instanceof SpeedIncreasePellet){
                            speedIncreaseEffect(movableEntity);
                        }
                        if(entity instanceof SpeedDecreasePellet){
                            speedDecreaseEffect(movableEntity);
                        }
                        if(entity instanceof InvisiblePellet){
                            invisiblePelletEffect(movableEntity);
                        }
                    }
                }
                // Ajouter à la liste de suppression
                entityToRemove.add(entity);

                // Augmenter la masse de l'entité
                double newMass = movableEntity.getMass() + entity.getMass();
                movableEntity.setMass(newMass);

            }
        }
        // Supprimer les pellets mangés
        for (Entity entity : entityToRemove) {
            quadTree.removeNode(entity, quadTree);
            if (entity instanceof IA)
                robots.remove(entity);
            entities.remove(entity);
            entitiesCircles.remove(entity);
        }
    }

    public void animatePelletConsumption(Entity pellet, MovableEntity p) {
        try{
            TranslateTransition transition = new TranslateTransition();
            transition.setNode(entitiesCircles.get(pellet));
            transition.setDuration(Duration.millis(50));
            transition.setToX(p.getPosX() - entitiesCircles.get(pellet).getCenterX());
            transition.setToY(p.getPosY() - entitiesCircles.get(pellet).getCenterY());
            transition.setAutoReverse(true);
            transition.setInterpolator(Interpolator.EASE_OUT);
            transition.play();
        }
        catch(NullPointerException e){
            System.out.println("Elément mangé alors qu'il n'a pas encore été instancié");
        }
    }

    public void eatPlayer(){
        this.isPlayerAlive = false;

        ButtonType exit = new ButtonType("Quitter", ButtonBar.ButtonData.APPLY);
        Alert alert = new Alert(Alert.AlertType.NONE, "Vous êtes mort ! Veuillez recommencer.", exit);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.APPLY){
            Platform.exit();
        }
        alert.setOnCloseRequest(e -> Platform.exit());
        Platform.exit();
    }

    public boolean isPlayerAlive(){
        return isPlayerAlive;
    }

    public double getSpecialSpeed(){
        return this.specialSpeed;
    }

    public void invisiblePelletEffect(MovableEntity movableEntity) {
        if(entitiesCircles.get(movableEntity) != null) {
            Paint color = entitiesCircles.get(movableEntity).getFill();
            new Thread(() -> {
                entitiesCircles.get(movableEntity).setFill(Color.TRANSPARENT);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                entitiesCircles.get(movableEntity).setFill(color);
            }).start();
        }
    }

    public void speedIncreaseEffect(MovableEntity movableEntity){
        new Thread(() -> {
            this.specialSpeed = 15;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.specialSpeed = -1;
        }).start();
    }

    public void speedDecreaseEffect(MovableEntity movableEntity){
        new Thread(() -> {
            this.specialSpeed = 2;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.specialSpeed = -1;
        }).start();
    }
}
