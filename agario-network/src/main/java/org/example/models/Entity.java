package org.example.models;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public abstract class Entity {

    private static int id = 0;
    private DoubleProperty posX = new SimpleDoubleProperty();
    private DoubleProperty posY = new SimpleDoubleProperty();
    private double mass;
    private DoubleProperty radius = new SimpleDoubleProperty();

    public Entity(double x, double y, double mass){
        this.id = ++this.id;
        this.posX.set(x);
        this.posY.set(y);
        this.mass = mass;
        this.radius.set(10*Math.sqrt(this.mass));
    }

    public double getRadius() {
        return radius.get();
    }

    public void setRadius(double targetRadius) {
        this.radius.set(targetRadius);
    }

    public int getId() {
        return id;
    }

    public double getMass() {
        return mass;
    }

    public double getPosX() {
        return posX.get();
    }

    public double getPosY() {
        return posY.get();
    }

    public DoubleProperty getPosXProperty() {
        return posX;
    }

    public DoubleProperty getPosYProperty() {
        return posY;
    }

    public DoubleProperty getRadiusProperty() {
        return radius;
    }

    public void setMass(double mass) {
        this.mass = mass;
        this.setRadius(10*Math.sqrt(this.mass));
    }

    public void setPosX(double posX) {
        this.posX.set(posX);
    }

    public void setPosY(double posY) {
        this.posY.set(posY);
    }

    public void setId(int id) {
        this.id = id;
    }
}