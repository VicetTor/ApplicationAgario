package com.example.agario.models.utils;

import com.example.agario.models.Player;

public class Camera extends Dimension {

    private Player player;
    private double zoomFactor = 1.0;

    public Camera(Player player) {
        super(
                player.getPosX() - 100 * Math.sqrt(player.getRadius()) / 2,  // xMin (gauche)
                player.getPosY() - 100 * Math.sqrt(player.getRadius()) / 2,  // yMin (haut)
                player.getPosX() + 100 * Math.sqrt(player.getRadius()) / 2,  // xMax (droite)
                player.getPosY() + 100 * Math.sqrt(player.getRadius()) / 2   // yMax (bas)
        );
        this.player = player;
    }

    // Method to update the camera's dimensions based on the player's radius and zoom factor
    public void updateCameraDimensions() {
        double baseViewSize = 50;

        double viewSize = baseViewSize * Math.max(1, Math.sqrt(player.getRadius() / 80.0)); //Mettre 1 pour voir toute la carte

        this.zoomFactor = viewSize / baseViewSize;

        double halfView = viewSize / 2;
        this.setxMin(player.getPosX() - halfView);
        this.setyMin(player.getPosY() - halfView);
        this.setxMax(player.getPosX() + halfView);
        this.setyMax(player.getPosY() + halfView);
    }


    public double getTop() {
        return getyMin();
    }

    public double getZoomFactor() {
        return zoomFactor;
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

    public double getPositionX() {
        return this.getLeft();
    }

    public double getPositionY() {
        return this.getTop();
    }

    public double getWidth() {
        return this.getRight() - this.getLeft();
    }

    public double getHeight() {
        return this.getBottom() - this.getTop();
    }
}
