package com.example.agario.controllers;

import com.example.agario.input.PlayerInput;
import com.example.agario.models.*;
import com.example.agario.models.factory.PlayerFactory;
import com.example.agario.models.Entity;
import com.example.agario.models.Player;
import com.example.agario.models.Game;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class GameController implements Initializable {
    @FXML private TextField TchatTextField;
    @FXML private Pane GamePane;
    @FXML private ListView LeaderBoardListView;
    @FXML private ListView TchatListView;

    private final Map<Entity, String> pelletColors = new HashMap<>();

    private Game gameModel;

    // Taille plus grande de la carte
    public final int HEIGHT = 2000;
    public final int WIDTH = 2000;
    private Player player;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialisation");

        Dimension dimension = new Dimension(0, 0, WIDTH, HEIGHT);
        player = (Player) new PlayerFactory("GreatPlayer7895", WIDTH, HEIGHT).launchFactory();
        gameModel = new Game(new QuadTree(0, dimension), player);

        gameModel.createRandomPellets();

        PlayerInput playerInput = new PlayerInput();

        GamePane.setOnMouseMoved(playerInput);

        new Thread(() -> {
            AtomicReference<Double> dx = new AtomicReference<>(playerInput.getMouseX() - player.getPosX());
            AtomicReference<Double> dy = new AtomicReference<>(playerInput.getMouseY() - player.getPosY());
            while (true) {
                GamePane.setOnMouseMoved(e -> {
                    playerInput.handle(e);
                    dx.set(playerInput.getMouseX() - player.getPosX());
                    dy.set(playerInput.getMouseY() - player.getPosY());
                });
                System.out.println(dx);
                player.setSpeed(playerInput.getMouseX(), playerInput.getMouseY(), WIDTH, HEIGHT);

                player.updatePosition(dx.get(), dy.get(),WIDTH, HEIGHT);

                for (Entity robot : gameModel.getRobots()){
                    if(robot instanceof IA){
                        ((IA) robot).IAstart();
                    }
                }

                Platform.runLater(() -> {
                    double offsetX = getPaneWidth() / 2 - player.getPosX();
                    double offsetY = getPaneHeight() / 2 - player.getPosY();
                    GamePane.setTranslateX(offsetX);
                    GamePane.setTranslateY(offsetY);
                    List<Entity> pelletsList = new ArrayList<>();

                    Dimension cameraView = new Dimension(
                            -GamePane.getTranslateX(),
                            -GamePane.getTranslateY(),
                            -GamePane.getTranslateX() + getPaneWidth(),
                            -GamePane.getTranslateY() + getPaneHeight()
                    );

                    gameModel.getQuadTree().generatePelletsIfNeeded(cameraView, 20);

                    QuadTree.DFSChunk(gameModel.getQuadTree(), cameraView, pelletsList);

                    for (Entity robot : gameModel.getRobots()){
                        if(robot instanceof IA){
                            gameModel.eatPellet(pelletsList, (IA) robot);
                        }
                    }
                    gameModel.eatPellet(pelletsList, player);

                    GamePane.getChildren().clear();
                    displayPlayer();
                    displayPellets(pelletsList);
                    displayRobot(gameModel.getRobots());
                });

                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void displayPellets(List<Entity> pelletsList) {
        List<String> colors = List.of("#951b8a", "#4175ba", "#12b1af");

        for (Entity pellet : pelletsList) {
            Circle pelletCircle = new Circle();

            pelletColors.putIfAbsent(pellet, colors.get(new Random().nextInt(colors.size())));

            pelletCircle.setFill(Paint.valueOf(pelletColors.get(pellet)));
            pelletCircle.centerXProperty().bind(pellet.getPosXProperty());
            pelletCircle.centerYProperty().bind(pellet.getPosYProperty());
            pelletCircle.radiusProperty().bind(pellet.getRadiusProperty());

            GamePane.getChildren().add(pelletCircle);
        }
    }

    public void displayPlayer() {
        Circle playerCircle = new Circle();

        playerCircle.setFill(Paint.valueOf("#251256"));
        playerCircle.centerXProperty().bindBidirectional(player.getPosXProperty());
        playerCircle.centerYProperty().bindBidirectional(player.getPosYProperty());
        playerCircle.radiusProperty().bindBidirectional(player.getRadiusProperty());

        GamePane.getChildren().add(playerCircle);
    }

    public void displayRobot(List<Entity> robotsList) {
        for (Entity robot : robotsList) {
            Circle robotCircle = new Circle();

            robotCircle.setFill(Paint.valueOf("#8cb27a"));
            robotCircle.centerXProperty().bindBidirectional(robot.getPosXProperty());
            robotCircle.centerYProperty().bindBidirectional(robot.getPosYProperty());
            robotCircle.radiusProperty().bindBidirectional(robot.getRadiusProperty());

            GamePane.getChildren().add(robotCircle);
        }
    }

    public double getPaneWidth(){return GamePane.getWidth();}

    public double getPaneHeight(){return GamePane.getHeight(); }


    /*public void ecoute() {
        PlayerInput playerInput = new PlayerInput();
        Camera cam = new Camera(player);
        List<Entity> liste = new ArrayList<>();
        Game gameModel= new Game(new QuadTree(1, new Dimension(0,0,getPaneWidth(),getPaneHeight())));
        QuadTree.DFSChunk(gameModel.getQuadTree() ,cam , liste );



        new Thread(()->{
            playerInput.setMouseX(getPaneWidth()/2);
            playerInput.setMouseY(getPaneHeight()/2);
            while(true){
                GamePane.setOnMouseMoved(playerInput);
                player.setSpeed(playerInput.getMouseX(), playerInput.getMouseY(), getPaneWidth(), getPaneHeight());

                System.out.println("Player position: " + player.getPosX() + ", " + player.getPosY());

                List<Double> lst = cam.updateCameraPosition(getPaneWidth(), getPaneHeight());
                GamePane.setTranslateX(lst.get(0));
                GamePane.setTranslateY(lst.get(1));

                player.updatePosition(playerInput.getMouseX(), playerInput.getMouseY());

                System.out.println("NewX : " + player.getPosX() + " NewY : " + player.getPosY());
                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

    }*/
}