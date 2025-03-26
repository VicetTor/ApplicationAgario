package com.example.agario.utils;

import com.example.agario.models.Entity;
import com.example.agario.models.MovableEntity;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

import java.util.HashMap;

public class MiniMap {

    HashMap<Entity, Circle> entities;
    private final double xMap;
    private final double yMap;
    private double xWorld;
    private double yWorld;

    public MiniMap(double xW, double yW, double xM, double yM){
        this.yWorld = yW;
        this.xWorld = xW;
        this.xMap = xM;
        this.yMap = yM;
    }

    /*public double getXPlayerMap(DoubleProperty xPlayer){
        return (xMap*xPlayer.getValue())/xWorld;
    }

    public double getYPlayerMap(double yPlayer){
        return (yMap*yPlayer)/yWorld;
    }*/

    /*public void setEntities(HashMap<Entity, Circle> entities) {
        this.entities = new HashMap<>();
        entities.forEach((e,c) ->{
            if(e instanceof MovableEntity){
                this.entities.put(e,c);
            }
        });
        setPointOnMap();
    }


    public void setPointOnMap(){
        entities.forEach((e,c) ->{
            Circle circle = new Circle();
            circle.setFill(c.getFill());
            circle.centerXProperty().bind(e.getPosXProperty().multiply(xMap / xWorld));
            circle.centerYProperty().bind(e.getPosYProperty().multiply(yMap / yWorld));
            circle.radiusProperty().bind(e.getRadiusProperty().divide(10));
            if (!map.getChildren().contains(circle)) {
                map.getChildren().add(circle);
            }
        });
    }*/
}
