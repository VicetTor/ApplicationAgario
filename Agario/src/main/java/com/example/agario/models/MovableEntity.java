package com.example.agario.models;

public class MovableEntity extends Entity{

    private String name;
    private final double initialSpeed = 1;
    private double speed = initialSpeed;

    public MovableEntity(double x, double y, double mass) {
        super(x, y, mass);
    }


    public void setSpeed(double xCursor, double yCursor){
        double dx = xCursor - this.getPosX();
        double dy = yCursor - this.getPosY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        double maxSpeed = (initialSpeed+15 - (this.getMass()/2));
        double minSpeed = 2;

        this.speed = Math.max(minSpeed, Math.min(maxSpeed, distance / 10));
    }



    public void updatePosition(double xCursor, double yCursor){
        double currentPosXPoint = this.getPosX();
        double currentPosYPoint = this.getPosY();

        double dx = xCursor - currentPosXPoint;
        double dy = yCursor - currentPosYPoint;

        double distanceEuclidienne = Math.sqrt(dx * dx + dy * dy);

        if (distanceEuclidienne < 1) return;

        double dirX = dx / distanceEuclidienne;
        double dirY = dy / distanceEuclidienne;

        double adjustedSpeed = Math.min(speed, distanceEuclidienne / 5);

        this.setPosX(currentPosXPoint + dirX * adjustedSpeed);
        this.setPosY(currentPosYPoint + dirY * adjustedSpeed);

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
