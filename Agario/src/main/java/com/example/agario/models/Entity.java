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

    public Entity(double x, double y, double mass) {
        this.id = idCounter.incrementAndGet();
        this.posX = x;
        this.posY = y;
        this.mass = mass;
        this.radius = 10 * Math.sqrt(this.mass);
    }

    public int getId() {
        return id;
    }

    public double getMass() {
        return mass;
    }

    public double getRadius() {
        return radius;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setMass(double mass) {
        this.mass = mass;
        this.radius = 10 * Math.sqrt(this.mass);
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }
}
