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

    public CameraController(Camera camera, List<Player> player, Pane gamePane){
        this.camera = camera;
        this.player = player;
        this.gamePane = gamePane;
    }

    public void applyCameraTransform(double paneWidth, double paneHeight) {
        double scale = 1.0 / camera.getZoomFactor();
        double screenCenterX = paneWidth / 2;
        double screenCenterY = paneHeight / 2;

        double translateX = screenCenterX - (player.get(0).getPosX() * scale);
        double translateY = screenCenterY - (player.get(0).getPosY() * scale);

        gamePane.getTransforms().clear();
        gamePane.getTransforms().addAll(
                new Translate(translateX, translateY),
                new Scale(scale, scale, 0, 0)
        );
    }

    public List<Entity> getVisibleEntities(QuadTree quadTree, List<Entity> robots, double paneWidth, double paneHeight) {
        List<Entity> visibleEntities = new ArrayList<>();
        double scale = 1.0 / camera.getZoomFactor();
        double translateX = (paneWidth / 2) - (player.get(0).getPosX() * scale);
        double translateY = (paneHeight / 2) - (player.get(0).getPosY() * scale);

        double inverseScale = 1.0 / scale;
        Dimension cameraView = new Dimension(
                -translateX * inverseScale,
                -translateY * inverseScale,
                (-translateX + paneWidth) * inverseScale,
                (-translateY + paneHeight) * inverseScale
        );

        QuadTree.DFSChunk(quadTree, cameraView, visibleEntities);
        visibleEntities.addAll(robots);

        return visibleEntities;
    }
}
