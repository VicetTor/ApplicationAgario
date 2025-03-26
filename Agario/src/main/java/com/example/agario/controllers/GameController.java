package com.example.agario.controllers;

import com.example.agario.input.PlayerInput;
import com.example.agario.models.*;
import com.example.agario.models.factory.PlayerFactory;
import com.example.agario.utils.Camera;
import com.example.agario.utils.Dimension;
import com.example.agario.utils.QuadTree;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class GameController implements Initializable {

    @FXML private Pane map;
    @FXML private TextField TchatTextField;
    @FXML private Pane GamePane;
    @FXML private AnchorPane OuterPane;
    @FXML private ListView<String> LeaderBoardListView;
    @FXML private ListView<String> TchatListView;
    @FXML private GridPane gridPane;
    @FXML private BorderPane GameBorderPane;

    private Map<Entity, Circle> entityCircles = new HashMap<>();
    private Map<Entity, String> pelletColors = new HashMap<>();
    private HashMap<Entity, Circle> entitiesMap = new HashMap<>();
    private Game gameModel;
    private Player player;
    private Stage stage;

    private static final int HEIGHT = 10000;
    private static final int WIDTH = 10000;
    private static final List<String> PELLET_COLORS = List.of("#ff3107", "#4e07ff", "#caff07","#ff07dc","#7107ff","#07ff2b","#07ff88","#07ff88","#07a8ff","#ff3107","#ff4f00","#ffff00");
    private static final String PLAYER_COLOR = "#7107ff";
    private static final String ROBOT_COLOR = "#07ff82";

    private final List<Entity> visibleEntities = Collections.synchronizedList(new ArrayList<>());


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupGame();
        startGameLoop();
        startPelletSpawner();
    }

    private void setupGame() {
        // Initialize player and game world
        player = (Player) new PlayerFactory("GreatPlayer7895", WIDTH, HEIGHT).launchFactory();

        // Setup game pane
        GamePane.setMinSize(WIDTH, HEIGHT);
        setupBackground();

        // Initialize game model
        Dimension dimension = new Dimension(0, 0, WIDTH, HEIGHT);
        gameModel = new Game(new QuadTree(0, dimension), player);
        gameModel.createRandomPellets(10000);
    }

    private void setupBackground() {
        GamePane.setStyle(null);
        Image backgroundImage = new Image(getClass().getResource("/com/example/agario/quadrillage.png").toExternalForm());
        BackgroundImage bgImg = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT
        );
        GamePane.setBackground(new Background(bgImg));
        GamePane.toFront();
        GameBorderPane.setStyle("-fx-background-color:#d8504d;");
    }

    private void startGameLoop() {
        PlayerInput playerInput = new PlayerInput();
        Camera camera = new Camera(player);
        GamePane.setOnMouseMoved(playerInput);

        new Thread(() -> {
            AtomicReference<Double> dx = new AtomicReference<>(0.0);
            AtomicReference<Double> dy = new AtomicReference<>(0.0);

            while (true) {
                // Update mouse position
                GamePane.setOnMouseMoved(e -> {
                    playerInput.handle(e);
                    dx.set(playerInput.getMouseX() - player.getPosX());
                    dy.set(playerInput.getMouseY() - player.getPosY());
                });

                // Update positions
                player.updatePosition(dx.get(), dy.get(), GamePane.getWidth(), GamePane.getHeight());
                updateRobots();

                Platform.runLater(() -> {
                    setEntities((HashMap<Entity, Circle>) entityCircles);
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

    private void updateRobots() {
        List<Entity> robotsCopy = new ArrayList<>(gameModel.getRobots());
        for (Entity robot : robotsCopy) {
            if (robot instanceof IA) {
                ((IA) robot).setPositionIA();
            }
        }
    }

    private void updateLeaderBoard(){
        int counter = 0;
        LeaderBoardListView.getItems().clear();
        List<Entity> allPlayers = new ArrayList<>(gameModel.getRobots());
        allPlayers.add(player);
        allPlayers.sort(new Comparator<Entity>() {
            @Override
            public int compare(Entity e1, Entity e2) {
                return Double.compare(e2.getMass(), e1.getMass());
            }
        });
        for(Entity entity : allPlayers){
            counter++;
            LeaderBoardListView.getItems().add("N°"+counter+" - Joueur "+entity.getId()+", score : "+entity.getMass());
            if(counter == 10) break;
        }

    }

    private void updateGameDisplay(Camera camera, double dx, double dy) {
        // Get a copy of the list to avoid concurrent modification
        List<Entity> visibleEntities = new ArrayList<>(getVisibleEntities(camera));

        // Clear previous frame
        GamePane.getChildren().clear();

        // Update camera
        camera.updateCameraDimensions();

        // Apply camera transformations
        applyCameraTransform(camera);

        // Update player speed
        player.setSpeed(dx, dy, stage.getHeight() / 2, stage.getWidth() / 2);

        // Handle collisions
        //gameModel.eatEntity(visibleEntities, player, this);

        for (Entity robot : new ArrayList<>(gameModel.getRobots())) {
            if (robot instanceof IA) {
                int cameraSize = 50;
                Dimension robotView = new Dimension(robot.getPosX()-cameraSize, robot.getPosY()-cameraSize,robot.getPosX()+cameraSize,robot.getPosY()+cameraSize);
                List<Entity> robotZone = new ArrayList<>();
                QuadTree.DFSChunk(gameModel.getQuadTree(), robotView, robotZone);
                robotZone.addAll(gameModel.getRobots());
                gameModel.eatEntity(robotZone, (MovableEntity) robot, this);
            }
        }
        // Render all entities
        renderEntities(visibleEntities);

        // Update leaderboard
        updateLeaderBoard();

        // Render all entities
        renderEntities(visibleEntities);

        // Handle collisions
        gameModel.eatEntity(visibleEntities, player, this);
    }


    public void animatePelletConsumption(Entity pellet) {
        TranslateTransition transition = new TranslateTransition();
        transition.setNode(entityCircles.get(pellet));
        transition.setDuration(Duration.millis(50));
        transition.setToX(player.getPosX() - entityCircles.get(pellet).getCenterX());
        transition.setToY(player.getPosY() - entityCircles.get(pellet).getCenterY());
        transition.setAutoReverse(true);
        transition.setInterpolator(Interpolator.EASE_OUT);
        transition.play();
    }


    private void applyCameraTransform(Camera camera) {
        double scale = 1.0 / camera.getZoomFactor();
        double screenCenterX = getPaneWidth() / 2;
        double screenCenterY = getPaneHeight() / 2;

        double translateX = screenCenterX - (player.getPosX() * scale);
        double translateY = screenCenterY - (player.getPosY() * scale);

        GamePane.getTransforms().clear();
        GamePane.getTransforms().addAll(
                new Translate(translateX, translateY),
                new Scale(scale, scale, 0, 0)
        );
    }

    private List<Entity> getVisibleEntities(Camera camera) {
        List<Entity> visibleEntities = new ArrayList<>();
        double scale = 1.0 / camera.getZoomFactor();
        double translateX = (getPaneWidth() / 2) - (player.getPosX() * scale);
        double translateY = (getPaneHeight() / 2) - (player.getPosY() * scale);

        double inverseScale = 1.0 / scale;
        Dimension cameraView = new Dimension(
                -translateX * inverseScale,
                -translateY * inverseScale,
                (-translateX + getPaneWidth()) * inverseScale,
                (-translateY + getPaneHeight()) * inverseScale
        );

        QuadTree.DFSChunk(gameModel.getQuadTree(), cameraView, visibleEntities);
        visibleEntities.addAll(gameModel.getRobots());
        return visibleEntities;
    }

    private void renderEntities(List<Entity> entities) {
        // Render pellets first
        entities.stream()
                .filter(e -> !(e instanceof Player) && !(e instanceof IA))
                .forEach(this::renderPellet);

        // Render robots
        entities.stream()
                .filter(e -> e instanceof IA)
                .forEach(this::renderRobot);

        // Render player on top
        renderPlayer();
    }

    private void renderPellet(Entity pellet) {
        Circle circle = entityCircles.computeIfAbsent(pellet, k -> {
            Circle c = new Circle();
            String color = PELLET_COLORS.get(new Random().nextInt(PELLET_COLORS.size()));
            pelletColors.put(pellet, color);
            c.setFill(Paint.valueOf(color));
            return c;
        });

        updateCircle(circle, pellet);
        GamePane.getChildren().add(circle);
    }

    private void renderRobot(Entity robot) {
        Circle circle = entityCircles.computeIfAbsent(robot, k -> {
            Circle c = new Circle();
            c.setFill(Paint.valueOf(ROBOT_COLOR));
            return c;
        });

        updateCircle(circle, robot);
        GamePane.getChildren().add(circle);
    }

    private void renderPlayer() {
        Circle circle = entityCircles.computeIfAbsent(player, k -> {
            Circle c = new Circle();
            c.setFill(Paint.valueOf(PLAYER_COLOR));
            return c;
        });

        updateCircle(circle, player);
        GamePane.getChildren().add(circle);
    }

    private void updateCircle(Circle circle, Entity entity) {
        circle.setCenterX(entity.getPosX());
        circle.setCenterY(entity.getPosY());
        circle.setRadius(entity.getRadius());
    }

    public void setEntities(HashMap<Entity, Circle> entities) {
        this.entitiesMap = new HashMap<>();
        entities.forEach((e,c) ->{
            if(e instanceof MovableEntity){
                this.entitiesMap.put(e,c);
            }
        });
        updateMiniMap(entitiesMap);
    }

    public void updateMiniMap(HashMap<Entity, Circle> entities){
        map.getChildren().clear();

        Rectangle square = new Rectangle(50, 50);
        square.setFill(null);
        square.setStroke(Color.RED);
        square.setStrokeWidth(1);

        double centerX = (player.getPosX() * map.getPrefWidth()) / WIDTH;
        double centerY = (player.getPosY() * map.getPrefHeight()) / HEIGHT;

        square.setX(centerX - square.getWidth() / 2);
        square.setY(centerY - square.getHeight() / 2);

        map.getChildren().add(square);

        double x1Square = player.getPosX()-1400;
        double x2Square = player.getPosX()+1400;
        double y1Square = player.getPosY()+1800;
        double y2Square = player.getPosY()-1800;

        entities.forEach((e,c) ->{

            double posXE = c.getCenterX();
            double posYE = c.getCenterY();

            if (posXE >= x1Square && posXE <= x2Square && posYE <= y1Square && posYE >= y2Square){
                Circle circle = new Circle();
                circle.setFill(c.getFill());
                circle.setCenterX((posXE * map.getPrefWidth()) / WIDTH );
                circle.setCenterY((posYE * map.getPrefHeight()) / HEIGHT);
                circle.setRadius( e.getRadius()/14 );
                if (!map.getChildren().contains(circle)) {
                    map.getChildren().add(circle);
                }
            }
        });

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

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private double getPaneWidth() {
        return GamePane.getWidth();
    }

    private double getPaneHeight() {
        return GamePane.getHeight();
    }
}