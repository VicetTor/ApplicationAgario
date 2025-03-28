package com.example.agario.client.controllers;

import com.example.agario.models.Entity;
import com.example.agario.models.MovableEntity;
import com.example.agario.models.Player;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiniMapController {

    private Map<Entity, Circle> entitiesCircles;
    private Pane map;

    public MiniMapController(Map<Entity, Circle> entitiesCircles, Pane map){
        this.entitiesCircles = entitiesCircles;
        this.map = map;
    }


    public void updateMiniMap(List<Player> player, double width, double height){
        HashMap<Entity, Circle> entities = new HashMap<Entity, Circle>();

        entitiesCircles.forEach((e,c) ->{
            if(e instanceof MovableEntity){
                entities.put(e,c);
            }
        });

        map.getChildren().clear();

        Rectangle square = new Rectangle(50, 50);
        square.setFill(null);
        square.setStroke(Color.RED);
        square.setStrokeWidth(1);

        double averageXPlayer = 0;
        double averageYPlayer = 0;
        for (Player p : player){
            averageXPlayer += p.getPosX();
            averageYPlayer += p.getPosY();
        }

        averageXPlayer = averageXPlayer/player.size();
        averageYPlayer = averageYPlayer/player.size();

        double centerX = (averageXPlayer * map.getPrefWidth()) / width;
        double centerY = (averageYPlayer * map.getPrefHeight()) / height;

        square.setX(centerX - square.getWidth() / 2);
        square.setY(centerY - square.getHeight() / 2);

        map.getChildren().add(square);

        double x1Square = averageXPlayer-1400;
        double x2Square = averageXPlayer+1400;
        double y1Square = averageYPlayer+1800;
        double y2Square = averageYPlayer-1800;

        entities.forEach((e,c) ->{

            double posXE = c.getCenterX();
            double posYE = c.getCenterY();

            if (posXE >= x1Square && posXE <= x2Square && posYE <= y1Square && posYE >= y2Square){
                Circle circle = new Circle();
                circle.setFill(c.getFill());
                circle.setCenterX((posXE * map.getPrefWidth()) / width );
                circle.setCenterY((posYE * map.getPrefHeight()) / height);
                circle.setRadius( e.getRadius()/18 );
                if (!map.getChildren().contains(circle)) {
                    map.getChildren().add(circle);
                }
            }
        });
    }
}
