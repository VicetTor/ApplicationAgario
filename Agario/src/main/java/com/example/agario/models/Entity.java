package com.example.agario.models;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Entity implements Serializable {
    private static final long serialVersionUID = -1329751809095675046L;
    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private final int id;
    protected double posX;
    protected double posY;
    protected double mass;
    protected double radius;
    protected String nom;

    /**
     * constructor for entity
     * @param x x position
     * @param y y position
     * @param mass weight of the entity
     */
    public Entity(double x, double y, double mass) {
        this.id = idCounter.incrementAndGet();
        this.posX = x;
        this.posY = y;
        this.mass = mass;
        this.radius = 10 * Math.sqrt(this.mass);
    }

    /**
     * id getter
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * mass getter
     * @return mass
     */
    public double getMass() {
        return mass;
    }

    /**
     * radius getter
     * @return radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * x position getter
     * @return x position
     */
    public double getPosX() {
        return posX;
    }

    /**
     * y position getter
     * @return y position
     */
    public double getPosY() {
        return posY;
    }

    /**
     * mass setter
     * @param mass weight to apply
     */
    public void setMass(double mass) {
        this.mass = mass;
        this.radius = 10 * Math.sqrt(this.mass);
    }

    /**
     * x position setter
     * @param posX x position to apply
     */
    public void setPosX(double posX) {
        this.posX = posX;
    }

    /**
     * y position setter
     * @param posY y position to apply
     */
    public void setPosY(double posY) {
        this.posY = posY;
    }
}
