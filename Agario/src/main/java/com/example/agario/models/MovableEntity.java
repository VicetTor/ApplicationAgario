package com.example.agario.models;

public class MovableEntity extends Entity{

    private String name;
    private double speed = 10;

    public MovableEntity(double x, double y, double mass) {
        super(x, y, mass);
    }


    public void setSpeed(double xCursor, double yCursor, double screenWidth, double screenHeight){
        double percentageX = xCursor/screenWidth;
        double percentageY = yCursor/screenHeight;
        double maxSpeed = 150 - this.getMass(); // vitesse maximale à ajuster selon le rendu, plus la pastille est grosse plus elle est lente (vitesse à l'apparition - masse)
        double speedX = Math.abs(percentageX) * maxSpeed;
        double speedY = Math.abs(percentageY) * maxSpeed;
        this.speed = (speedX + speedY)/2;
    }



    public void updatePosition(double xCursor, double yCursor){
        double currentPosXPoint = this.getPosX();
        double currentPosYPoint = this.getPosY();

        double dx = xCursor - currentPosXPoint;
        double dy = yCursor - currentPosYPoint;

        double moyenneX = xCursor;
        double moyenneY = yCursor;

        double distanceEuclidienne = Math.sqrt(dx * dx + dy * dy);

        while(distanceEuclidienne > speed){
            moyenneX = ((moyenneX + currentPosXPoint)/2);
            moyenneY = ((moyenneY + currentPosYPoint)/2);

            dx = moyenneX - currentPosXPoint;
            dy = moyenneY - currentPosYPoint;

            distanceEuclidienne = Math.sqrt(dx * dx + dy * dy);
        }

        this.setPosX(moyenneX);
        this.setPosY(moyenneY);

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
