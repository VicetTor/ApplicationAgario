package com.example.agario.controllers;

import com.example.agario.input.PlayerInput;
import com.example.agario.models.Entity;
import com.example.agario.models.Game;
import com.example.agario.models.Player;
import com.example.agario.models.PlayerFactory;
import com.example.agario.utils.Camera;
import com.example.agario.utils.Dimension;
import com.example.agario.utils.QuadTree;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.util.ResourceBundle;

public class GameController implements Initializable {
    @FXML private TextField TchatTextField;
    @FXML private Pane GamePane;
    @FXML private ListView LeaderBoardListView;
    @FXML private ListView TchatListView;

    private Game gameModel;

    // Taille plus grande de la carte
    public final int HEIGHT = 2000;
    public final int WIDTH = 2000;
    private Dimension dimension;
    private Player player;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialisation");

        // Ajustement de la taille de la carte
        dimension = new Dimension(0, 0, WIDTH, HEIGHT);
        gameModel = new Game(new QuadTree(0,dimension));
        gameModel.createRandomPellets();

        this.player = (Player) new PlayerFactory("GreatPlayer7895").launchFactory();
        gameModel.getQuadTree().insertNode(player);

        PlayerInput playerInput = new PlayerInput();
        Camera cam = new Camera(player);


        GamePane.setOnMouseMoved(playerInput);

        new Thread(() -> {
            while (true) {
                player.setSpeed(playerInput.getMouseX(), playerInput.getMouseY(), WIDTH, HEIGHT);

                // Mise à jour de la position de la caméra
                // Mise à jour de la position du joueur
                player.updatePosition(playerInput.getMouseX(), playerInput.getMouseY());

                Platform.runLater(() -> {
                    double offsetX = getPaneWidth() / 2 - player.getPosX();
                    double offsetY = getPaneHeight() / 2 - player.getPosY();
                    GamePane.setTranslateX(offsetX);
                    GamePane.setTranslateY(offsetY);
                    List<Entity> liste = new ArrayList<>();
                    Dimension cameraView = new Dimension(
                            -GamePane.getTranslateX(),
                            -GamePane.getTranslateY(),
                            -GamePane.getTranslateX() + getPaneWidth(),
                            -GamePane.getTranslateY() + getPaneHeight()
                    );

                    // Générer des pellets dans le QuadTree si nécessaire
                    gameModel.getQuadTree().generatePelletsIfNeeded(cameraView, 5);

                    // Récupérer les pellets dans la zone visible
                    QuadTree.DFSChunk(gameModel.getQuadTree(), cameraView, liste);

                    GamePane.getChildren().clear();
                    displayPlayer();
                    displayPellets(liste);
                });


                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public double getPaneWidth(){return GamePane.getWidth();}

    public double getPaneHeight(){return GamePane.getHeight(); }

    public void displayPellets(List<Entity> liste){
        for(Entity pellet : liste){
            Circle pelletCircle = new Circle();

            List<String> colors = new ArrayList<>();
            colors.add("#951b8a");colors.add("#4175ba");colors.add("#12b1af");

            pelletCircle.setFill(Paint.valueOf("#736fad"));
            pelletCircle.centerXProperty().bind(pellet.getPosXProperty());
            pelletCircle.centerYProperty().bind(pellet.getPosYProperty());
            pelletCircle.radiusProperty().bind(pellet.getRadiusProperty());

            GamePane.getChildren().add(pelletCircle);
        }
    }

    public void displayPlayer(){
        Circle playerCircle = new Circle();
        playerCircle.setFill(Paint.valueOf("#251256"));
        playerCircle.centerXProperty().bindBidirectional(player.getPosXProperty());
        playerCircle.centerYProperty().bindBidirectional(player.getPosYProperty());
        playerCircle.radiusProperty().bindBidirectional(player.getRadiusProperty());
        GamePane.getChildren().add(playerCircle);
    }

}