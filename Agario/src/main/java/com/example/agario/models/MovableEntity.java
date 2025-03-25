package com.example.agario.models;

public class MovableEntity extends Entity{

    private String name;
    private double speed = 100;

    public MovableEntity(double x, double y, double mass) {
        super(x, y, mass);
    }

    public void setSpeed(double xCursor, double yCursor){ //on image que l'écran est 100 * 100
        double maxSpeed = 150 - super.getMass(); // vitesse maximale à ajuster selon le rendu, plus la pastille est grosse plus elle est lente (vitesse à l'apparition - masse)
        double speedX = Math.abs(xCursor) * maxSpeed;
        double speedY = Math.abs(yCursor) * maxSpeed;
        this.speed = (speedX + speedY)/2;
    }

    public double getSpeed() {
        return speed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
