package com.example.agario.client.controllers;

import com.example.agario.models.*;
import com.example.agario.models.specialPellet.InvisiblePellet;
import com.example.agario.models.specialPellet.SpecialPellet;
import com.example.agario.models.specialPellet.SpeedDecreasePellet;
import com.example.agario.models.specialPellet.SpeedIncreasePellet;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RenderController {

    private Map<Entity, Circle> entitiesCircles;
    private List<Player> player;
    private Pane gamePane;


    private static final List<String> PELLET_COLORS = List.of("#ff3107", "#4e07ff", "#caff07","#ff07dc","#7107ff","#07ff2b","#07ff88","#07ff88","#07a8ff","#ff3107","#ff4f00","#ffff00");
    private static final String PLAYER_COLOR = "#7107ff";
    private static final String ROBOT_COLOR = "#07ff82";
    private Map<Entity, String> pelletColors = new HashMap<>();


    /**
     * Constructor of the RenderController
     *
     * @param entitiesCircles hashmap of entities link to their circle in the game
     * @param gamePane pane of the game
     * @param player players' instance
     */
    public RenderController(Map<Entity, Circle> entitiesCircles, Pane gamePane, List<Player> player){
        this.entitiesCircles = entitiesCircles;
        this.gamePane = gamePane;
        this.player = player;
    }

    /**
     * add and update forms of the entities during the game
     *
     * @param entities entities in the game
     */
    public void renderEntities(List<Entity> entities) {
        // Render pellets first
        entities.stream()
                .filter(e -> !(e instanceof Player) && !(e instanceof IA))
                .forEach(this::renderPellet);

        // Render robots
        entities.stream()
                .filter(e -> e instanceof IA)
                .forEach(this::renderRobot);

        // Render player on top
        renderPlayer(player);
    }

    /**
     * add and update forms of the pellets during the game
     *
     * @param pellet pellet
     */
    private void renderPellet(Entity pellet) {
        if(pellet instanceof SpecialPellet){
            Circle circle = entitiesCircles.computeIfAbsent(pellet, k -> {
                Circle c = new Circle();
                String color = "";

                if(pellet instanceof InvisiblePellet) color = "#b5dbe8";
                else if(pellet instanceof SpeedIncreasePellet) color = "#ffff99";
                else color = "#ff0000";

                pelletColors.put(pellet, color);
                c.setFill(Paint.valueOf(color));
                c.setOpacity(0.5);

                if(pellet instanceof InvisiblePellet) c.setStroke(Color.BLANCHEDALMOND);
                else if(pellet instanceof SpeedIncreasePellet) c.setStroke(Color.YELLOW);
                else c.setStroke(Color.RED);

                DropShadow glow = new DropShadow();

                if(pellet instanceof InvisiblePellet) glow.setColor(Color.BLUE);
                else if(pellet instanceof SpeedIncreasePellet)glow.setColor(Color.YELLOW);
                else glow.setColor(Color.RED);

                glow.setRadius(10);
                c.setEffect(glow);
                return c;
            });
            updateCircle(circle, pellet);
            gamePane.getChildren().add(circle);
        }
        else {
            Circle circle = entitiesCircles.computeIfAbsent(pellet, k -> {
                Circle c = new Circle();
                String color = PELLET_COLORS.get(new Random().nextInt(PELLET_COLORS.size()));
                pelletColors.put(pellet, color);
                c.setFill(Paint.valueOf(color));
                return c;
            });
            updateCircle(circle, pellet);
            gamePane.getChildren().add(circle);
        }
    }

    /**
     * add and update forms of the robots during the game
     *
     * @param robot robot
     */
    private void renderRobot(Entity robot) {
        Circle circle = entitiesCircles.computeIfAbsent(robot, k -> {
            Circle c = new Circle();
            c.setFill(Paint.valueOf(ROBOT_COLOR));
            return c;
        });

        updateCircle(circle, robot);
        gamePane.getChildren().add(circle);
    }

    /**
     * add and update form of the player during the game
     *
     * @param player instances' player
     */
    private void renderPlayer(List<Player> player) {
        for(Player p : player) {
            Circle circle = entitiesCircles.computeIfAbsent(p, k -> {
                Circle c = new Circle();
                c.setFill(Paint.valueOf(PLAYER_COLOR));
                return c;
            });
            Label l = new Label(p.getName());
            l.setLabelFor(circle);
            l.setTextFill(Color.WHITE);
            l.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            DropShadow shadow = new DropShadow();
            shadow.setOffsetX(1);
            shadow.setOffsetY(1);
            shadow.setColor(Color.BLACK);
            l.setEffect(shadow);

            l.widthProperty().addListener((obs, oldVal, newVal) ->
                    l.setLayoutX(circle.getCenterX() - newVal.doubleValue() / 2)
            );

            circle.centerXProperty().addListener((obs, oldVal, newVal) ->
                    l.setLayoutX(newVal.doubleValue() - l.getWidth() / 2)
            );
            circle.centerYProperty().addListener((obs, oldVal, newVal) ->
                    l.setLayoutY(newVal.doubleValue() - (l.getHeight()/2) -10)
            );

            //animatePlayerMovement(circle, p.getPosX(), p.getPosY());

            updateCircle(circle, p);
            gamePane.getChildren().add(circle);
            gamePane.getChildren().add(l);
        }
    }

    /**
     * add and update forms of the players during the online game
     *
     * @param otherPlayer player on online game
     */
    public void renderOtherPlayer(Player otherPlayer) {
        Circle circle = entitiesCircles.computeIfAbsent(otherPlayer, k -> {
            Circle c = new Circle();
            c.setFill(Paint.valueOf("#FF0000")); // Rouge pour les autres joueurs
            return c;
        });
        updateCircle(circle, otherPlayer);
        gamePane.getChildren().add(circle);
    }

    /**
     *
     * update circles' position in the game
     *
     * @param circle circle
     * @param entity entity linked to the circle
     */
    private void updateCircle(Circle circle, Entity entity) {
        circle.setCenterX(entity.getPosX());
        circle.setCenterY(entity.getPosY());
        circle.setRadius(entity.getRadius());
    }
}
