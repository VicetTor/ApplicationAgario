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

/**
 * controller which controls the flow of the game
 */
public class GameController implements Initializable {

    //FXML attributes
    @FXML private Pane map;
    @FXML private TextField tchatTextField;
    @FXML private Pane gamePane;
    @FXML private AnchorPane outerPane;
    @FXML private ListView<String> leaderBoardListView;
    @FXML private ListView<String> tchatListView;
    @FXML private GridPane gridPane;  //TODO aucune utilité ?
    @FXML private BorderPane gameBorderPane;
    @FXML private Button buttonSettings;

    //attributes
    private Map<Entity, Circle> entitiesCircles = new HashMap<>();
    private Map<Entity, String> pelletColors = new HashMap<>();
    private HashMap<Entity, Circle> entitiesMap = new HashMap<>();
    private Game gameModel;
    private List<Player> player = new ArrayList<Player>();
    private int timer = -1;
    private Stage stage;
    private List<Double> specialSpeed = new ArrayList<Double>();
    private boolean isPlayerAlive = true;

    //CONTROLLERS attributes
    private CameraController cameraController;
    private MiniMapController miniMapController;
    private AnimationController animationController;
    private RenderController renderController;
    private AbsorptionController absorptionController;
    private boolean transitionSplit = false;

    //SETTINGS attributes
    private static int HEIGHT = 10000;
    private static int WIDTH = 10000;
    private static int ROBOT_NUMBER = 25;
    private static int PELLET_NUMBER = 5000;
    private static String PLAYER_NAME = "Anonymous";

    //SERVER attributes
    private List<Player> otherPlayers = new ArrayList<>();
    private ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Socket socket;


    /**
     * Initialize the view of the game
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupGame();
        startGameLoop();
        startPelletSpawner();
    }

    /**
     * set the game's parameters
     */
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
        this.specialSpeed.add(-1.0);
        this.player.add(gameModel.getPlayer());
        gameModel.createRandomPellets(PELLET_NUMBER);
    }

    /**
     * set the background of the game field
     */
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

    /**
     * start the game : the player can play the game
     */
    private void startGameLoop() {
        PlayerInput playerInput = new PlayerInput();
        Camera camera = new Camera(gameModel.getPlayer());
        this.cameraController = new CameraController(camera, player, gamePane);

        gamePane.setOnMouseMoved(playerInput);
        gamePane.setOnMouseClicked(event -> {

            splitPlayer(playerInput);

        });

        //Thread which manages the split of the player
        new Thread(() -> {
            while (true) {
                synchronized (player) {
                    try {
                        timer -= 1;

                        if(isPlayerAlive) {
                            if (timer < (int) (10 + (player.get(0).getMass() / 100)) - 2) {
                                Iterator<Player> iterator = player.iterator();
                                while (iterator.hasNext()) {
                                    Player p = iterator.next();

                                    for (Player p2 : new ArrayList<>(player)) {
                                        if (p2 != p) {
                                            double distance = Math.sqrt(Math.pow(p.getPosX() - p2.getPosX(), 2) + Math.pow(p.getPosY() - p2.getPosY(), 2));
                                            double threshold = (p.getRadius() + p2.getRadius()) * 0.80;

                                            if (distance <= threshold) {
                                                if (player.get(0) != p2) {
                                                    if (p.getSpeed() != 30 && p2.getSpeed() != 30) {
                                                        double newMass = p.getMass() + p2.getMass();

                                                        double newPosX = (p.getPosX() * p.getMass() + p2.getPosX() * p2.getMass()) / newMass;
                                                        double newPosY = (p.getPosY() * p.getMass() + p2.getPosY() * p2.getMass()) / newMass;


                                                        p.setMass(newMass);
                                                        p.setPosX(newPosX);
                                                        p.setPosY(newPosY);


                                                        int indexP2 = player.indexOf(p2);
                                                        if (indexP2 != -1) {
                                                            entitiesCircles.remove(p2);
                                                            specialSpeed.remove(indexP2);
                                                            player.remove(p2);

                                                            timer = (int) (10 + (p.getMass() / 100));
                                                            if (player.size() <= 1) {
                                                                timer = -1;
                                                            }

                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            timer = -1;
                            Thread.interrupted();
                        }


                        Thread.sleep(50);

                    } catch (ConcurrentModificationException e) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (timer == 0) {

                    double sommeMasse = 0;
                    for (Player pl : player) {
                        sommeMasse += pl.getMass();
                    }

                    double averageXPlayer = 0;
                    double averageYPlayer = 0;
                    for (Player p : player) {
                        averageXPlayer += p.getPosX();
                        averageYPlayer += p.getPosY();
                    }

                    averageXPlayer = averageXPlayer / player.size();
                    averageYPlayer = averageYPlayer / player.size();

                    player.get(0).setMass(sommeMasse);
                    player.get(0).setPosX(averageXPlayer);
                    player.get(0).setPosY(averageYPlayer);

                    int size = player.size();

                    Platform.runLater(() -> {
                        for (int i = size - 1; i >= 1; i--) {
                            entitiesCircles.remove(player.get(i));
                            specialSpeed.remove(i);
                            player.remove(i);

                        }
                    });

                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            }).start();


        //Update positions of entities in the game
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

                Platform.runLater(() -> {
                    miniMapController.updateMiniMap(player, WIDTH, HEIGHT);
                    updateGameDisplay(camera, dx.get(), dy.get());
                });


                try {
                    Thread.sleep(33); // ~30 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * update the robots' position
     */
    private void updateRobots() {
        List<Entity> robotsCopy = new ArrayList<>(gameModel.getRobots());
        for (Entity robot : robotsCopy) {
            if (robot instanceof IA) {
                ((IA) robot).setPositionIA();
            }
        }
    }

    /**
     * update the display of the game
     *
     * @param camera camera which follows the player
     * @param dx coordinate x of the player
     * @param dy coordinate y of the player
     */
    private void updateGameDisplay(Camera camera, double dx, double dy) {
        // Get a copy of the list to avoid concurrent modification
        List<Entity> visibleEntities = new ArrayList<>(cameraController.getVisibleEntities(gameModel.getQuadTree(), gameModel.getRobots(), getPaneWidth(), getPaneHeight(), isPlayerAlive));
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
        int i = 0;
        for(Player p : player) {
            p.setSpeed(dx, dy, stage.getHeight() / 2, stage.getWidth() / 2, specialSpeed.get(i));
            i++;
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
                if(isPlayerAlive) {
                    for (Player p : player) {
                        robotZone.add(p);
                    }
                }
                absorptionController.eatEntity(robotZone, (MovableEntity) robot, gameModel.getQuadTree(), gameModel.getRobots(), player);
                isPlayerAlive = absorptionController.isPlayerAlive();
            }
        }

        // Player absorbs other entities
        if(isPlayerAlive) {
            for (Player p : player) {
                absorptionController.eatEntity(visibleEntities, p, gameModel.getQuadTree(), gameModel.getRobots(), player);
                specialSpeed = absorptionController.getSpecialSpeed();
                isPlayerAlive = absorptionController.isPlayerAlive();
            }
        }

        // Update leaderboard
        updateLeaderBoard();
    }


    /**
     * update the leaderboard of the game, with the top ten players
     */
    private void updateLeaderBoard() {
        int counter = 0;
        leaderBoardListView.getItems().clear();

        List<Entity> allPlayers = new ArrayList<>(gameModel.getRobots());
        if (isPlayerAlive) allPlayers.add(player.get(0));
        allPlayers.sort(new Comparator<Entity>() {
            @Override
            public int compare(Entity e1, Entity e2) {
                return Double.compare(e2.getMass(), e1.getMass());
            }
        });
        for (Entity entity : allPlayers) {
            counter++;
            MovableEntity joueur = (MovableEntity) entity;
            leaderBoardListView.getItems().add(counter + ". " + joueur.getName() + "     " + new DecimalFormat("0.00").format(joueur.getMass()));
            if (counter == 10) break;
        }
        leaderBoardListView.setMinHeight(counter);

        if (gameModel.getRobots().size() == 5) {
            robotSpawner(5);
        }
    }

    /**
     * open the settings' menu
     */
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

    /**
     * add 2 pellets in different places in the map, every second
     */
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

    /**
     * add robots to the game
     *
     * @param limite number of robots to create
     */
    private void robotSpawner(int limite) {
        gameModel.createRandomRobots(limite);
    }

    /**
     * set instance of player
     *
     * @param player player of the game
     */
    public void setPlayer(Player player){
        this.player.set(0,player);
    }

    /**
     * functionality to split the player during the game
     *
     * @param playerInput the inputs made by the player
     */
    public void splitPlayer(PlayerInput playerInput) {

        double splitDistance = 75;

        List<Player> temporaryListPlayer = new ArrayList<>(player);

        for (Player p : temporaryListPlayer) {
            double weightPlayerDivided = p.getMass() / 2;

            if (weightPlayerDivided > 20) {
                p.setMass(weightPlayerDivided);

                double angle = Math.atan2(playerInput.getMouseY() - p.getPosY(), playerInput.getMouseX() - p.getPosX());
                double offsetX = Math.cos(angle) * splitDistance;
                double offsetY = Math.sin(angle) * splitDistance;

                Player newP = new Player(p.getPosX() +offsetX, p.getPosY() + offsetY, p.getName());
                newP.setMass(weightPlayerDivided);

                double speed = newP.getSpeed();

                specialSpeed.add(-1.0);
                player.add(newP);

                timer = (int) (10 + (newP.getMass()/100));

                new Thread(() -> {

                    this.specialSpeed.set(this.player.indexOf(newP),30.0);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(this.player.indexOf(newP)!=-1) {
                            this.specialSpeed.set(this.player.indexOf(newP), speed);
                        }
                }).start();
            }
        }
    }


    /**
     *
     * getters of the attributes
     *
     */
    private double getPaneWidth() {return gamePane.getWidth();}

    private double getPaneHeight() {return gamePane.getHeight();}

    public static int getHeight() { return HEIGHT; }

    public static int getWidth() {return WIDTH;}

    public static int getPelletNumber() {return PELLET_NUMBER;}

    public static int getRobotNumber() {return ROBOT_NUMBER;}

    public static String getPlayerName() {return PLAYER_NAME;}

    /**
     *
     * setters of the attributes
     *
     */
    public void setStage(Stage stage) {this.stage = stage;}

    public static void setHeight(int HEIGHT) {GameController.HEIGHT = HEIGHT;}

    public static void setWidth(int WIDTH) {GameController.WIDTH = WIDTH;}

    public static void setPlayerName(String playerName) {PLAYER_NAME = playerName;}

    public static void setPelletNumber(int pelletNumber) {PELLET_NUMBER = pelletNumber;}

    public static void setRobotNumber(int robotNumber) {ROBOT_NUMBER = robotNumber;}
}