package com.example.agario.models;

public class MovableEntity extends Entity{

    private String name;
    private final double initialSpeed = 10;
    private double speed = initialSpeed;
    private double dirX = 0;
    private double dirY = 0;


    public MovableEntity(double x, double y, double mass) {
        super(x, y, mass);
    }

    public void setSpeed(double dx, double dy, double width, double height){

        double maxDistanceCursor = width;
        if (maxDistanceCursor < height) maxDistanceCursor = height;

        double distancePlayerCursor = Math.sqrt(dx * dx + dy * dy);

        double percentageDistance = distancePlayerCursor/(maxDistanceCursor/2);

        double maxSpeed = (initialSpeed  *  15/(this.getMass()*0.5) );
        //System.out.println(maxSpeed);
        if(maxSpeed > initialSpeed){
            maxSpeed = initialSpeed;
        }
        double minSpeed = 1;

        this.speed = Math.max(minSpeed, maxSpeed*percentageDistance);
    }

    public void updatePosition(double dx, double dy, double screenWidth, double screenHeight){

        double distanceEuclidienne = Math.sqrt(dx * dx + dy * dy);


        if (distanceEuclidienne > 1) {
            dirX = dx / distanceEuclidienne;
            dirY = dy / distanceEuclidienne;
        }
        speed = 10 ;

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
        // System.out.println(a);
        this.setPosX(q);
        this.setPosY(a);
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