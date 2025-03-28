package com.example.agario.client.controllers;

import com.example.agario.models.Entity;
import com.example.agario.models.Player;
import com.example.agario.models.utils.Camera;
import com.example.agario.models.utils.Dimension;
import com.example.agario.models.utils.QuadTree;
import javafx.scene.layout.Pane;
import javafx.scene.robot.Robot;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.ArrayList;
import java.util.List;

public class CameraController {

    private Camera camera;
    private List<Player> player;
    private Pane gamePane;

    /**
     * Constructor of CameraController
     *
     * @param camera camera of the player
     * @param player instances' of player
     * @param gamePane gaming field
     */
    public CameraController(Camera camera, List<Player> player, Pane gamePane){
        this.camera = camera;
        this.player = player;
        this.gamePane = gamePane;
    }

    /**
     * Apply the change of camera to the player
     *
     * @param paneWidth width of visible gaming field
     * @param paneHeight height of visible gaming field
     */
    public void applyCameraTransform(double paneWidth, double paneHeight) {
        double scale = 1.0 / camera.getZoomFactor();
        double screenCenterX = paneWidth / 2;
        double screenCenterY = paneHeight / 2;

        double averageXPlayer = 0;
        double averageYPlayer = 0;
        for (Player p : player){
            averageXPlayer += p.getPosX();
            averageYPlayer += p.getPosY();
        }

        averageXPlayer = averageXPlayer/player.size();
        averageYPlayer = averageYPlayer/player.size();

        double translateX = screenCenterX - (averageXPlayer * scale);
        double translateY = screenCenterY - (averageYPlayer * scale);

        gamePane.getTransforms().clear();
        gamePane.getTransforms().addAll(
                new Translate(translateX, translateY),
                new Scale(scale, scale, 0, 0)
        );
    }


    /**
     * Return the list of visible entities in a given dimension
     *
     * @param quadTree data structure with the pellets of the game
     * @param robots robots' list of the game
     * @param paneWidth width of visible gaming field
     * @param paneHeight height of visible gaming field
     * @param isPlayerAlive true if the player is alive else false
     * @return
     */
    public List<Entity> getVisibleEntities(QuadTree quadTree, List<Entity> robots, double paneWidth, double paneHeight, boolean isPlayerAlive) {
        List<Entity> visibleEntities = new ArrayList<>();
        double scale = 1.0 / camera.getZoomFactor();

        double averageXPlayer = 0;
        double averageYPlayer = 0;
        for (Player p : player){
            averageXPlayer += p.getPosX();
            averageYPlayer += p.getPosY();
        }

        averageXPlayer = averageXPlayer/player.size();
        averageYPlayer = averageYPlayer/player.size();

        double translateX = (paneWidth / 2) - (averageXPlayer  * scale);
        double translateY = (paneHeight / 2) -  (averageYPlayer  * scale);

        double inverseScale = 1.0 / scale;
        Dimension cameraView = new Dimension(
                -translateX * inverseScale,
                -translateY * inverseScale,
                (-translateX + paneWidth) * inverseScale,
                (-translateY + paneHeight) * inverseScale
        );

        QuadTree.DFSChunk(quadTree, cameraView, visibleEntities);
        visibleEntities.addAll(robots);
        if(isPlayerAlive)
            for(Player p : player) {
                visibleEntities.add(p);
            }

        return visibleEntities;
    }
}
