package com.example.agario.models;

public class MovableEntity extends Entity{

    private String name;
    private final double initialSpeed = 1;
    private double speed = initialSpeed;
    private double dirX = 0;
    private double dirY = 0;


    public MovableEntity(double x, double y, double mass) {
        super(x, y, mass);
    }


    public void setSpeed(double xCursor, double yCursor, double width, double height){
        double dx = xCursor - width;
        double dy = yCursor - height;
        double distance = Math.sqrt(dx * dx + dy * dy);

        double maxSpeed = (initialSpeed+15 - (this.getMass()/2));
        double minSpeed = 2;

        this.speed = Math.max(minSpeed, Math.min(maxSpeed, distance / 10));
    }

    public void updatePosition(double dx, double dy, double screenWidth, double screenHeight){

       // double dx = xCursor - this.getPosX();
        //double dy = yCursor - this.getPosY();

        double distanceEuclidienne = Math.sqrt(dx * dx + dy * dy);


        if (distanceEuclidienne > 1) {
            dirX = dx / distanceEuclidienne;
            dirY = dy / distanceEuclidienne;
        }

        // double adjustedSpeed = Math.min(speed, distanceEuclidienne / 5);

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
