package com.example.agario.models;

public class MovableEntity extends Entity{

    private String name;
    private double speedX = 10;
    private double speedY = 10;

    public MovableEntity(double x, double y, double mass) {
        super(x, y, mass);
    }

    public void setSpeed(double xCursor, double yCursor, double screenWidth, double screenHeight){ //on image que l'écran est 100 * 100

        double maxSpeed = (this.speedX / this.getMass()); // vitesse maximale à ajuster selon le rendu, plus la pastille est grosse plus elle est lente (vitesse à l'apparition - masse)

        double centerX = screenWidth / 2;
        double centerY = screenHeight / 2;

        double dx = xCursor - centerX;
        double dy = yCursor - centerY; //Distance entre les deux

        double distance = Math.sqrt(dx * dx + dy * dy); //euclidienne

        if(distance < 10) {//à voir en fonction de la taille de la boule
            this.speedX = 0;
            this.speedY = 0;
            return;
        }

        double directionX = dx / distance;//direction vers le point
        double directionY = dy / distance;

        this.speedX = 2;
        this.speedY = 2;

    }

    public void updatePosition(double xCursor, double yCursor){
        setPosX(xCursor);
        setPosY(yCursor);
    }

    /*
    On veut récupérer la current position du point et la position du curseur
    Ensuite récupère la vitesse et la masse pour faire le calcul de déplacement
    On avance vers le curseur de la valeur du calcul du déplacement.
    calcul de la distance euclidienne

    différence entre x et y des curseurs et points
    tu prends ta vitesse en px et t'augmentes ton x et ton y de ta vitesse
    exemple : curseur 30px point 5px alors si vitesse 5px on va vers le x en faisant : 10 15 20 25 30


   dans le cas ou la position du curseur est inéfrieure à la speed alors on prend le px de la distance.


     */

    public double getSpeedX() {
        return speedX;
    }

    public double getSpeedY(){
        return speedY;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
