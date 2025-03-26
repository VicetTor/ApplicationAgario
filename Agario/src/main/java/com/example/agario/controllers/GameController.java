package com.example.agario.controllers;

import com.example.agario.input.PlayerInput;
import com.example.agario.models.*;
import com.example.agario.models.factory.PlayerFactory;
import com.example.agario.utils.Camera;
import com.example.agario.utils.Dimension;
import com.example.agario.utils.QuadTree;
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
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class GameController implements Initializable {
    @FXML private TextField TchatTextField;
    @FXML private Pane GamePane;
    @FXML private AnchorPane OuterPane;
    @FXML private ListView<String> LeaderBoardListView;
    @FXML private ListView<String> TchatListView;
    @FXML private GridPane gridPane;
    @FXML private BorderPane GameBorderPane;

    private final Map<Entity, Circle> entityCircles = new HashMap<>();
    private final Map<Entity, String> pelletColors = new HashMap<>();
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
                System.out.println("NB ROBOTS ="+gameModel.getRobots().size());
                // Update mouse position
                GamePane.setOnMouseMoved(e -> {
                    playerInput.handle(e);
                    dx.set(playerInput.getMouseX() - player.getPosX());
                    dy.set(playerInput.getMouseY() - player.getPosY());
                });

                // Update positions
                player.updatePosition(dx.get(), dy.get(), GamePane.getWidth(), GamePane.getHeight());
                updateRobots();

                // Update display
                Platform.runLater(() -> updateGameDisplay(camera, dx.get(), dy.get()));

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
        LeaderBoardListView.getItems().clear();
        List<Entity> allPlayers = new ArrayList<>(gameModel.getRobots());
        allPlayers.add(player);
        Collections.sort(allPlayers, new Comparator<Entity>() {
            @Override
            public int compare(Entity e1, Entity e2) {
                return Double.compare(e2.getMass(), e1.getMass());
            }
        });
        for(int i = 0; i < 10; i++){
            LeaderBoardListView.getItems().add(i+"- "+allPlayers.get(i).getId()+" = "+allPlayers.get(i).getMass());
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
        gameModel.eatEntity(visibleEntities, player);

        for (Entity robot : new ArrayList<>(gameModel.getRobots())) {
            if (robot instanceof IA) {
                int cameraSize = 50;
                Dimension robotView = new Dimension(robot.getPosX()-cameraSize, robot.getPosY()-cameraSize,robot.getPosX()+cameraSize,robot.getPosY()+cameraSize);
                List<Entity> robotZone = new ArrayList<>();
                QuadTree.DFSChunk(gameModel.getQuadTree(), robotView, robotZone);
                robotZone.addAll(gameModel.getRobots());
                gameModel.eatEntity(robotZone, (MovableEntity) robot);
            }
        }
        // Render all entities
        renderEntities(visibleEntities);
        System.out.println("NB ENTITES VISIBLES="+visibleEntities.size());

        // Update leaderboard
        updateLeaderBoard();
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