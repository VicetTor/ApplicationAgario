package com.example.agario.models;

public class MovableEntity extends Entity{

    private String name;
    private final double initialSpeed = 10;
    private double speed = initialSpeed;
    private double dirX = 0;
    private double dirY = 0;

    public void setNetworkSpeed(double speed) {
        this.speed = speed;
    }


    public double getCurrentSpeed() {
        return this.speed;
    }



    public MovableEntity(double x, double y, double mass) {
        super(x, y, mass);
    }

    public void setSpecialSpeed(double speed){
        this.speed = speed;
    }

    public void setSpeed(double dx, double dy, double x, double y, double specialSpeed){

        if (specialSpeed != -1){
            setSpecialSpeed(specialSpeed);
        }

        else{

            double distancePlayerCursor = Math.sqrt(dx * dx + dy * dy);

            double maxDistance = x;
            if (x>y) maxDistance = y;


            double percentageDistance = distancePlayerCursor/maxDistance;
            if (percentageDistance>1) percentageDistance = 1;

            double maxSpeed = (initialSpeed * 15/(this.getMass()*0.1));
            if(maxSpeed > initialSpeed){
                maxSpeed = initialSpeed;
            }

            double minSpeed = 1;

            this.speed = Math.max(minSpeed, maxSpeed*percentageDistance);
        }

    }

    public void updatePosition(double dx, double dy, double screenWidth, double screenHeight){

        double distanceEuclidienne = Math.sqrt(dx * dx + dy * dy);


        if (distanceEuclidienne > 1) {
            dirX = dx / distanceEuclidienne;
            dirY = dy / distanceEuclidienne;
        }


        double q = this.getPosX() + dirX * speed;
        double a = this.getPosY() + dirY * speed;

        // 🚀
        if (q <= 0) {
            q = 1;
        } else if (q >= screenWidth - 1) {
            q = screenWidth - 2;
        }

        if (a <= 0) {
            a = 1;
        } else if (a >= screenHeight - 1) {
            a = screenHeight - 2;
        }
        if(screenHeight != 0 && screenHeight != 0 ){
            // System.out.println(a);
            this.setPosX(q);
            this.setPosY(a);
        }

    }


    public double getDirX() { return dirX; }
    public double getDirY() { return dirY; }
    public void setDirX(double dirX) { this.dirX = dirX; }
    public void setDirY(double dirY) { this.dirY = dirY; }

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