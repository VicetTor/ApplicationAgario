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

        this.speed = 2;//Math.max(minSpeed, Math.min(maxSpeed, distance / 10));
    }

    public void updatePosition(double xCursor, double yCursor) {
        double dx = xCursor - this.getPosX();
        double dy = yCursor - this.getPosY();
        double distanceEuclidienne = Math.sqrt(dx * dx + dy * dy);

        // Vérification pour éviter la division par zéro
        if (distanceEuclidienne > 1) {
            dirX = dx / distanceEuclidienne;
            dirY = dy / distanceEuclidienne;
        }

        double newX = this.getPosX() + dirX * speed;
        double newY = this.getPosY() + dirY * speed;

        // Obtenir les dimensions réelles de la zone de jeu
        double maxX = 600;
        double maxY = 600;

        // Limiter le joueur aux bords du jeu
        newX = Math.max(0, Math.min(newX, maxX - 1));
        newY = Math.max(0, Math.min(newY, maxY - 1));

        // Mise à jour des coordonnées
        this.setPosX(newX);
        this.setPosY(newY);
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
