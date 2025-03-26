package com.example.agario.controllers;

import com.example.agario.Launcher;
import com.example.agario.input.PlayerInput;
import com.example.agario.models.Entity;
import com.example.agario.models.Player;
import com.example.agario.models.factory.PlayerFactory;
import com.example.agario.models.Game;
import com.example.agario.utils.Camera;
import com.example.agario.utils.Dimension;
import com.example.agario.utils.QuadTree;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class GameController implements Initializable {
    @FXML
    private TextField TchatTextField;
    @FXML
    private Pane GamePane;

    @FXML
    private AnchorPane OuterPane;
    @FXML
    private GridPane gridPane;

    @FXML
    private BorderPane GameBorderPane;
    @FXML
    private ListView LeaderBoardListView;
    @FXML
    private ListView TchatListView;

    private Map<Entity, String> pelletColors = new HashMap<>();

    private Game gameModel;

    // Taille plus grande de la carte
    public final int HEIGHT = 10000;
    public final int WIDTH = 10000;
    private Dimension dimension;
    private Player player;

    private Circle playerCircle;


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GamePane.setStyle("-fx-background-color:white;");
        GamePane.setMinWidth(WIDTH);
        GamePane.setMinHeight(HEIGHT);


        GameBorderPane.setStyle("-fx-background-color:#d8504d;");

        System.out.println("Initialisation");

        dimension = new Dimension(0, 0, WIDTH, HEIGHT);
        gameModel = new Game(new QuadTree(0,dimension));
        gameModel.createRandomPellets(500);

        this.player = (Player) new PlayerFactory("GreatPlayer7895", WIDTH, HEIGHT).launchFactory();
        gameModel.getQuadTree().insertNode(player);

        PlayerInput playerInput = new PlayerInput();
        Camera cam = new Camera(player);

        GamePane.setOnMouseMoved(playerInput);

        new Thread(()->{
            while(true) {
                gameModel.createRandomPellets(2);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(()->{
            while(true) {
                gameModel.createRandomPellets(2);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();



        new Thread(() -> {
            AtomicReference<Double> dx = new AtomicReference<>(playerInput.getMouseX() - player.getPosX());
            AtomicReference<Double> dy = new AtomicReference<>(playerInput.getMouseY() - player.getPosY());
            while (true) {
                GamePane.setOnMouseMoved(e -> {
                    System.out.println("sfsfdfd");
                    playerInput.handle(e);
                    dx.set(playerInput.getMouseX() - player.getPosX());
                    dy.set(playerInput.getMouseY() - player.getPosY());
                });
                player.updatePosition(dx.get(), dy.get(), GamePane.getWidth(), GamePane.getHeight());

                Platform.runLater(() -> {

                    cam.updateCameraDimensions();

                    double screenCenterX = getPaneWidth() / 2;
                    double screenCenterY = getPaneHeight() / 2;

                    double scale = 1.0 / cam.getZoomFactor();
                    double translateX = screenCenterX - (player.getPosX() * scale);
                    double translateY = screenCenterY - (player.getPosY() * scale);

                    GamePane.getTransforms().clear();
                    GamePane.getTransforms().addAll(
                            new Translate(translateX, translateY),
                            new Scale(scale, scale, 0, 0)
                    );

                    double x = stage.getHeight()/2;
                    double y = stage.getWidth()/2;

                    player.setSpeed(dx.get(), dy.get(), x,y);


                    double inverseScale = 1.0 / scale;
                    Dimension cameraView = new Dimension(
                            -translateX * inverseScale,
                            -translateY * inverseScale,
                            (-translateX + getPaneWidth()) * inverseScale,
                            (-translateY + getPaneHeight()) * inverseScale
                    );

                    List<Entity> entities = new ArrayList<>();
                    QuadTree.DFSChunk(gameModel.getQuadTree(), cameraView, entities);

                    gameModel.eatPellet(entities, player, playerCircle);

                    GamePane.getChildren().clear();
                    displayPlayer();
                    displayPellets(entities);
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

    public void displayPellets(List<Entity> liste) {
        List<String> colors = List.of("#951b8a", "#4175ba", "#12b1af");

        for (Entity pellet : liste) {
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
        playerCircle = new Circle();
        playerCircle.setFill(Paint.valueOf("#251256"));
        playerCircle.centerXProperty().bindBidirectional(player.getPosXProperty());
        playerCircle.centerYProperty().bindBidirectional(player.getPosYProperty());
        playerCircle.radiusProperty().bindBidirectional(player.getRadiusProperty());

        GamePane.getChildren().add(playerCircle);
    }



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

