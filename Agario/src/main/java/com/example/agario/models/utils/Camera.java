package com.example.agario.models.utils;

import com.example.agario.models.Player;

public class Camera extends Dimension {

    private Player player;
    private double zoomFactor = 1.0;

    /**
     * constructor for Camera
     * @param player the player to follow
     */
    public Camera(Player player) {
        super(
                player.getPosX() - 100 * Math.sqrt(player.getRadius()) / 2,  // xMin (gauche)
                player.getPosY() - 100 * Math.sqrt(player.getRadius()) / 2,  // yMin (haut)
                player.getPosX() + 100 * Math.sqrt(player.getRadius()) / 2,  // xMax (droite)
                player.getPosY() + 100 * Math.sqrt(player.getRadius()) / 2   // yMax (bas)
        );
        this.player = player;
    }

    /**
     * Method to update the camera's dimensions based on the player's radius and zoom factor
     */
    public void updateCameraDimensions() {
        double baseViewSize = 50;

        double viewSize = baseViewSize * Math.max(1, Math.sqrt(player.getRadius() / 80.0)); // default = 80.0 |Mettre 1 pour voir toute la carte

        this.zoomFactor = viewSize / baseViewSize;

        double halfView = viewSize / 2;
        this.setxMin(player.getPosX() - halfView);
        this.setyMin(player.getPosY() - halfView);
        this.setxMax(player.getPosX() + halfView);
        this.setyMax(player.getPosY() + halfView);
    }

    /**
     * top getter
     * @return double top of the camera
     */
    public double getTop() {
        return getyMin();
    }

    /**
     * zoom factor getter
     * @return double zoom factor
     */
    public double getZoomFactor() {
        return zoomFactor;
    }

    /**
     * left getter
     * @return double left of the camera
     */
    public double getLeft() {
        return getxMin();
    }

    /**
     * right getter
     * @return double right of the camera
     */
    public double getRight() {
        return getxMax();
    }

    /**
     * bottom getter
     * @return double bottom of the camera
     */
    public double getBottom() {
        return getyMax();
    }

    /**
     * x position getter
     * @return double x position of the camera
     */
    public double getPositionX() {
        return this.getLeft();
    }

    /**
     * y position getter
     * @return double y position of the camera
     */
    public double getPositionY() {
        return this.getTop();
    }

    /**
     * width getter
     * @return double width of the camera
     */
    public double getWidth() {
        return this.getRight() - this.getLeft();
    }

    /**
     * height getter
     * @return double height of the camera
     */
    public double getHeight() {
        return this.getBottom() - this.getTop();
    }

    /**
     * setter for player
     * @param localPlayer
     */
    public void setPlayer(Player localPlayer) {
        this.player=localPlayer;
    }
}
