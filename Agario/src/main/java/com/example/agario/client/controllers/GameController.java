package com.example.agario.client.controllers;


import com.example.agario.models.ConnectionResult;
import com.example.agario.client.GameClient;
import com.example.agario.client.PlayerInput;
import com.example.agario.models.*;
import com.example.agario.models.utils.Camera;
import com.example.agario.models.utils.Dimension;
import com.example.agario.models.utils.QuadTree;


import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;

import javafx.scene.control.*;

import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class GameController implements Initializable {

    //FXML
    @FXML private Pane map;
    @FXML private TextField tchatTextField;
    @FXML private Pane gamePane;
    @FXML private AnchorPane outerPane;
    @FXML private ListView<String> leaderBoardListView;
    @FXML private ListView<String> tchatListView;
    @FXML private GridPane gridPane;  //TODO aucune utilité ?
    @FXML private BorderPane gameBorderPane;
    @FXML private Button buttonSettings;

    private Map<Entity, Circle> entitiesCircles = new HashMap<>();
    private Game gameModel;
    private List<Player> player = new ArrayList<Player>();

    private Stage stage;
    private double specialSpeed = -1;
    private boolean isPlayerAlive = true;

    //CONTROLLERS
    private CameraController cameraController;
    private MiniMapController miniMapController;
    private AnimationController animationController;
    private RenderController renderController;
    private AbsorptionController absorptionController;

    //SETTINGS
    private static int HEIGHT = 10000;
    private static int WIDTH = 10000;
    private static int ROBOT_NUMBER = 25;
    private static int PELLET_NUMBER = 5000;
    private static String PLAYER_NAME = "Anonymous";

    //SERVER
    private List<Player> otherPlayers = new ArrayList<>();
    private ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Socket socket;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupGame();
        startGameLoop();
        startPelletSpawner();
    }

    private void setupGame() {
        // Setup game pane
        gamePane.setMinSize(WIDTH, HEIGHT);
        setupBackground();

        this.miniMapController = new MiniMapController(entitiesCircles, map);
        this.renderController = new RenderController(entitiesCircles, gamePane, player);
        this.absorptionController = new AbsorptionController(entitiesCircles, specialSpeed);

        // Initialize game model
        Dimension dimension = new Dimension(0, 0, WIDTH, HEIGHT);

        gameModel = new Game(new QuadTree(0, dimension), PLAYER_NAME, ROBOT_NUMBER);
        this.player.add(gameModel.getPlayer());
        gameModel.createRandomPellets(PELLET_NUMBER);
    }

    private void setupBackground() {
        gamePane.setStyle(null);
        Image backgroundImage = new Image(getClass().getResource("/com/example/agario/quadrillage.png").toExternalForm());
        BackgroundImage bgImg = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT
        );
        gamePane.setBackground(new Background(bgImg));
        gamePane.toFront();
        gameBorderPane.setStyle("-fx-background-color:#d8504d;");

        leaderBoardListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
                setTextFill(Paint.valueOf("#ffffff"));
                setBackground(Background.EMPTY);
                setStyle("-fx-background-color: transparent;");
                setPrefHeight(leaderBoardListView.getHeight()/10.5);
            }
        });
    }

    private void startGameLoop() {
        PlayerInput playerInput = new PlayerInput();
        Camera camera = new Camera(gameModel.getPlayer());
        this.cameraController = new CameraController(camera, player, gamePane);

        gamePane.setOnMouseMoved(playerInput);

        /*gamePane.setOnMouseClicked(event -> {
            splitPlayer();
            System.out.println("Clic détecté aux coordonnées : X=" + event.getX() + " Y=" + event.getY());
        });*/


        new Thread(() -> {
            AtomicReference<Double> dx = new AtomicReference<>(0.0);
            AtomicReference<Double> dy = new AtomicReference<>(0.0);

            while (true) {
                // Check if player is alive
                this.isPlayerAlive = absorptionController.isPlayerAlive();

                // Update mouse position
                for(Player p : player) {
                    if (isPlayerAlive) {
                        gamePane.setOnMouseMoved(e -> {
                            playerInput.handle(e);
                            dx.set(playerInput.getMouseX() - p.getPosX());
                            dy.set(playerInput.getMouseY() - p.getPosY());
                        });

                        // Update positions
                        p.updatePosition(dx.get(), dy.get(), gamePane.getWidth(), gamePane.getHeight());
                    }
                }
                updateRobots();

                sendPlayerUpdate();

                Platform.runLater(() -> {
                    miniMapController.updateMiniMap(player, WIDTH, HEIGHT);
                    updateGameDisplay(camera, dx.get(), dy.get());
                });


                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateRobots() {
        List<Entity> robotsCopy = new ArrayList<>(gameModel.getRobots());
        for (Entity robot : robotsCopy) {
            if (robot instanceof IA) {
                ((IA) robot).setPositionIA();
            }
        }
    }

    private void sendPlayerUpdate() {
        try {
            if (oos != null) {
                oos.writeObject(player);
                oos.flush();
                oos.reset(); // Important pour éviter les problèmes de cache
            }
        } catch (IOException e) {
            System.err.println("Erreur d'envoi au serveur");
        }
    }

    private void updateGameDisplay(Camera camera, double dx, double dy) {
        // Get a copy of the list to avoid concurrent modification
        List<Entity> visibleEntities = new ArrayList<>(cameraController.getVisibleEntities(gameModel.getQuadTree(), gameModel.getRobots(), getPaneWidth(), getPaneHeight()));
        if(isPlayerAlive)
            for(Player p : player) {
                visibleEntities.add(p);
            }

        // Clear previous frame
        gamePane.getChildren().clear();

        // Update camera
        camera.updateCameraDimensions();

        // Apply camera transformations
        cameraController.applyCameraTransform(getPaneWidth(), getPaneHeight());

        // Update player speed
        for(Player p : player) {
            p.setSpeed(dx, dy, stage.getHeight() / 2, stage.getWidth() / 2, specialSpeed);
        }
        // Render all entities
        renderController.renderEntities(visibleEntities);


        otherPlayers.forEach(otherPlayer -> {
            if (!otherPlayer.getName().equals(player.get(0).getName())) {
                renderController.renderOtherPlayer(otherPlayer);
            }
        });

        // Robots absorb other entities
        for (Entity robot : new ArrayList<>(gameModel.getRobots())) {
            if (robot instanceof IA) {
                int cameraRobotSize = 50;
                Dimension robotView = new Dimension(robot.getPosX()-cameraRobotSize, robot.getPosY()-cameraRobotSize,
                        robot.getPosX()+cameraRobotSize,robot.getPosY()+cameraRobotSize);
                List<Entity> robotZone = new ArrayList<>();
                QuadTree.DFSChunk(gameModel.getQuadTree(), robotView, robotZone);
                robotZone.addAll(gameModel.getRobots());

                if(isPlayerAlive) robotZone.add(player.get(0));

                absorptionController.eatEntity(robotZone, (MovableEntity) robot, gameModel.getQuadTree(), gameModel.getRobots());
                isPlayerAlive = absorptionController.isPlayerAlive();
            }
        }

        // Player absorbs other entities
        if(isPlayerAlive) {
            for (Player p : player) {
                absorptionController.eatEntity(visibleEntities, p, gameModel.getQuadTree(), gameModel.getRobots());
                specialSpeed = absorptionController.getSpecialSpeed();
                isPlayerAlive = absorptionController.isPlayerAlive();
            }
        }

        // Update leaderboard
        updateLeaderBoard();
    }

    private void updateLeaderBoard(){
        int counter = 0;
        leaderBoardListView.getItems().clear();

        List<Entity> allPlayers = new ArrayList<>(gameModel.getRobots());
        if(isPlayerAlive) allPlayers.add(player.get(0));
        allPlayers.sort(new Comparator<Entity>() {
            @Override
            public int compare(Entity e1, Entity e2) {
                return Double.compare(e2.getMass(), e1.getMass());
            }
        });
        for(Entity entity : allPlayers){
            counter++;
            MovableEntity joueur = (MovableEntity) entity;
            leaderBoardListView.getItems().add(counter+". "+joueur.getName()+"     "+new DecimalFormat("0.00").format(joueur.getMass()));
            if(counter == 10) break;
        }

        leaderBoardListView.setMinHeight(counter);

        if(gameModel.getRobots().size() == 5){
            robotSpawner(5);
        }
    }

    public void openSettingsMenuClick(){
        try {
            Stage oldWindowStage = (Stage) this.gameBorderPane.getScene().getWindow();
            oldWindowStage.close();

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/com/example/agario/settings.fxml"));
            SettingsController settingsController = fxmlLoader.getController();
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage newStage = new Stage();
            newStage.setTitle("Settings");
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            System.out.println("ERREUR");
        }
    }

    public void setupNetwork() {
        try {
            ConnectionResult connection = GameClient.playOnLine(player.get(0));
            if (connection == null) {
                throw new IOException("Échec de la connexion au serveur");
            }

            this.socket = connection.socket;
            this.oos = connection.oos;
            this.ois = connection.ois;

            // Démarrer le thread d'écoute des mises à jour
            startNetworkListener();

        } catch (IOException e) {
            e.printStackTrace();
            // Gestion d'erreur (fermer les flux, notifier l'utilisateur, etc.)
        }
    }

    private void startNetworkListener() {
        new Thread(() -> {
            try {
                while (true) {
                    Object received = ois.readObject();
                    if (received instanceof List) {
                        Platform.runLater(() -> {
                            otherPlayers.clear();
                            otherPlayers.addAll((List<Player>) received);
                        });
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Déconnexion du serveur");
            }
        }).start();
    }

    private void startPelletSpawner() {
        new Thread(() -> {
            while (true) {
                gameModel.createRandomPellets(2);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void robotSpawner(int limite) {
        gameModel.createRandomRobots(limite);
    }

    //TODO à quoi ça sert cette fonction ?
    /*public void setPlayer(Player player){
        this.player.set(0,player);
    }*/

    //TODO
    //Player Controller ?
    public void splitPlayer(){
        ArrayList<Player> newPlayer = new ArrayList<Player>();
        for (Player p : player){
            double weightPlayerDivided = p.getMass()/2;
            if (weightPlayerDivided > 20){
                Player newP = new Player(p.getPosX(),p.getPosY(),p.getName());
                newP.setMass(weightPlayerDivided); //*2
                newPlayer.add(p);
            }
        }
        this.player.clear();
        this.player = newPlayer;

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private double getPaneWidth() {return gamePane.getWidth();}

    private double getPaneHeight() {return gamePane.getHeight();}

    public static int getHeight() { return HEIGHT; }

    public static int getWidth() {return WIDTH;}

    public static int getPelletNumber() {return PELLET_NUMBER;}

    public static int getRobotNumber() {return ROBOT_NUMBER;}

    public static String getPlayerName() {return PLAYER_NAME;}


    public void setStage(Stage stage) {this.stage = stage;}

    public static void setHeight(int HEIGHT) {GameController.HEIGHT = HEIGHT;}

    public static void setWidth(int WIDTH) {GameController.WIDTH = WIDTH;}

    public static void setPlayerName(String playerName) {PLAYER_NAME = playerName;}

    public static void setPelletNumber(int pelletNumber) {PELLET_NUMBER = pelletNumber;}

    public static void setRobotNumber(int robotNumber) {ROBOT_NUMBER = robotNumber;}
}