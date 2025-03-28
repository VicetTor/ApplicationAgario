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
    private List<Double> specialSpeed;


    /**
     * Constructor of the AbsorptionController
     *
     * @param entitiesCircles hashmap of entities link to their circle in the game
     * @param specialSpeed speed given by special pellets
     */
    public AbsorptionController(Map<Entity, Circle> entitiesCircles, List<Double> specialSpeed){
        this.isPlayerAlive = true;
        this.entitiesCircles = entitiesCircles;
        this.specialSpeed = specialSpeed;
    }


    /**
     * Implements the functionality for the movable entity to eat and remove the eaten entity from all the lists
     *
     * @param entities list of visible entities to the movableEntity
     * @param movableEntity the entity eating
     * @param quadTree data structure with the pellets
     * @param robots list of the robots in the game
     * @param player actual player in the game
     */
    public void eatEntity(List<Entity> entities, MovableEntity movableEntity, QuadTree quadTree, List<Entity> robots, List<Player> player) {
        List<Entity> entityToRemove = new ArrayList<>();

        for (Entity entity : entities) {
            double dx = movableEntity.getPosX() - entity.getPosX();
            double dy = movableEntity.getPosY() - entity.getPosY();
            double squareDistance = dx * dx + dy * dy;

            if (squareDistance <= movableEntity.getRadius() * (movableEntity.getRadius() * 2)
                    && movableEntity.getMass() >= (entity.getMass() * 1.33)) {

                if (!(entity instanceof Player && (movableEntity.getName().equals(((Player) entity).getName())))) {
                    if (entity instanceof Player && !(movableEntity.getName().equals(((Player) entity).getName()))) {
                        this.eatPlayer((Player) entity, player);
                        break;
                    }

                    if (entity instanceof Pellet) {
                        if (movableEntity instanceof Player) {
                            this.animatePelletConsumption(entity, movableEntity);
                            if (entity instanceof SpeedIncreasePellet) {
                                speedIncreaseEffect(movableEntity, player);
                            }
                            if (entity instanceof SpeedDecreasePellet) {
                                speedDecreaseEffect(movableEntity, player);
                            }
                            if (entity instanceof InvisiblePellet) {
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

    /**
     * Animate the eating animation for the player
     *
     * @param pellet the eaten entity
     * @param p the player
     */
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

    /**
     * functionality which trigger the player's absorption or death
     *
     * @param playerEntity one instance of the player
     * @param player the list of the instances of player
     */
    public void eatPlayer(Player playerEntity, List<Player> player){
        entitiesCircles.remove(playerEntity);
        specialSpeed.remove(playerEntity);
        player.remove(playerEntity);

        if(player.size() == 0) {
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
    }

    /**
     * @return if the player is alive
     */
    public boolean isPlayerAlive(){
        return isPlayerAlive;
    }

    /**
     * @return the special speed given by the special pellets
     */
    public List<Double> getSpecialSpeed(){
        return this.specialSpeed;
    }

    /**
     * Give an invisible effect to the player
     *
     * @param movableEntity the entity who is given th effect
     */
    public void invisiblePelletEffect(MovableEntity movableEntity) {
        new Thread(() -> {
            if (entitiesCircles.get(movableEntity) != null) {
                entitiesCircles.get(movableEntity).setOpacity(0.2);
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (entitiesCircles.get(movableEntity) != null) {
                entitiesCircles.get(movableEntity).setOpacity(1);
            }
        }).start();
    }

    /**
     * Give a speed increase effect to the player
     *
     * @param movableEntity the entity who is given th effect
     * @param player instances' list of the player
     */
    public void speedIncreaseEffect(MovableEntity movableEntity, List<Player> player){
        new Thread(() -> {
            this.specialSpeed.set(player.indexOf(movableEntity),15.0);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.specialSpeed.set(player.indexOf(movableEntity),-1.0);
        }).start();
    }

    /**
     * Give a speed decrease effect to the player
     *
     * @param movableEntity the entity who is given th effect
     * @param player instances' list of the player
     */
    public void speedDecreaseEffect(MovableEntity movableEntity, List<Player> player){
        new Thread(() -> {
            this.specialSpeed.set(player.indexOf(movableEntity),2.0);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.specialSpeed.set(player.indexOf(movableEntity),-1.0);
        }).start();
    }
}
