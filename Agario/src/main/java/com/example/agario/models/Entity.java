package com.example.agario.models;

public abstract class Entity {

    private static int id = 0;
    private double posX;
    private double posY;
    private double mass;
    private double radius;

    public Entity(double x, double y, double mass){
        this.id = ++this.id;
        this.posX =x;
        this.posY =y;
        this.mass = mass;
        this.radius = 10*Math.sqrt(this.mass);
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public int getId() {
        return id;
    }

    public double getMass() {
        return mass;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public void setId(int id) {
        this.id = id;
    }
}
