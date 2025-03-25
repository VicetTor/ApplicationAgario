package com.example.agario.utils;

import com.example.agario.models.Player;

import java.util.ArrayList;
import java.util.List;

public class Camera extends Dimension {

    private Player player;

    // Constructeur de la caméra, ajusté selon la position et le rayon du joueur
    public Camera(Player player) {
        super(
                player.getPosX() - 100 * Math.sqrt(player.getRadius()) / 2,  // xMin (gauche)
                player.getPosY() - 100 * Math.sqrt(player.getRadius()) / 2,  // yMin (haut)
                player.getPosX() + 100 * Math.sqrt(player.getRadius()) / 2,  // xMax (droite)
                player.getPosY() + 100 * Math.sqrt(player.getRadius()) / 2   // yMax (bas)
        );
        this.player = player;
    }

    // Méthode pour mettre à jour la position de la caméra en fonction du mouvement du joueur
    public List<Double> updateCameraPosition(double width, double height) {
        double centerX = player.getPosX();
        double centerY = player.getPosY();

        // Calculer l'offset pour centrer la caméra sur le joueur
        double offsetX = centerX - width / 2;
        double offsetY = centerY - height / 2;

        // Retourner une liste avec les nouveaux décalages X et Y
        List<Double> list = new ArrayList<>();
        list.add(-offsetX);  // Décalage horizontal
        list.add(-offsetY);  // Décalage vertical
        return list;
    }

    // Méthode pour mettre à jour les dimensions de la caméra en fonction du rayon du joueur
    public void updateCameraDimensions() {
        // Ajuster les dimensions de la caméra en fonction du rayon du joueur
        double offset = 100 * Math.sqrt(player.getRadius()) / 2;

        // Modifier les valeurs de la caméra à l'aide des setters hérités de Dimension
        this.setxMin(player.getPosX() - offset);
        this.setyMin(player.getPosY() - offset);
        this.setxMax(player.getPosX() + offset);
        this.setyMax(player.getPosY() + offset);
    }

    // Méthodes pour obtenir les positions top, left, right et bottom de la caméra
    public double getTop() {
        return getyMin();
    }

    public double getLeft() {
        return getxMin();
    }

    public double getRight() {
        return getxMax();
    }

    public double getBottom() {
        return getyMax();
    }

    // Méthode pour obtenir la position X de la caméra
    public double getPositionX() {
        return this.getLeft();
    }

    // Méthode pour obtenir la position Y de la caméra
    public double getPositionY() {
        return this.getTop();
    }

    // Méthode pour obtenir la largeur de la caméra
    public double getWidth() {
        return this.getRight() - this.getLeft();
    }

    // Méthode pour obtenir la hauteur de la caméra
    public double getHeight() {
        return this.getBottom() - this.getTop();
    }
}
