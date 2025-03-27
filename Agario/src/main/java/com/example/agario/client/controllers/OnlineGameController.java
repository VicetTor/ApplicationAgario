package com.example.agario.client.controllers;

import com.example.agario.models.*;
import com.example.agario.models.utils.Camera;
import com.example.agario.models.utils.Dimension;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class OnlineGameController {
    @FXML private Pane map;
    @FXML private TextField TchatTextField;
    @FXML private Pane gamePane;
    @FXML private AnchorPane OuterPane;
    @FXML private ListView<String> LeaderBoardListView;
    @FXML private ListView<String> TchatListView;
    @FXML private GridPane gridPane;
    @FXML private BorderPane GameBorderPane;

    private Map<Entity, Circle> entitiesCircles = new HashMap<>();
    private Map<Entity, String> pelletColors = new HashMap<>();
    private HashMap<Entity, Circle> entitiesMap = new HashMap<>();
    private Player localPlayer;
    private List<Player> allPlayers = new ArrayList<>();

    private Stage stage;
    private double specialSpeed = -1;
    private boolean isPlayerAlive = true;

    private static final int HEIGHT = 10000;
    private static final int WIDTH = 10000;
    private static final List<String> PELLET_COLORS = List.of(
            "#ff3107", "#4e07ff", "#caff07", "#ff07dc", "#7107ff",
            "#07ff2b", "#07ff88", "#07a8ff", "#ff4f00", "#ffff00");
    private static final String PLAYER_COLOR = "#7107ff";
    private static final String OTHER_PLAYER_COLOR = "#ff0000";
    private static final String ROBOT_COLOR = "#07ff82";

    private ExecutorService networkExecutor = Executors.newCachedThreadPool();
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Socket socket;
    private AnimationTimer gameLoop;
    private Camera camera;

    private Map<String, Circle> playerCircles = new HashMap<>(); // Suivi des cercles des joueurs
    private Map<String, Label> playerLabels = new HashMap<>();  // Suivi des labels des joueurs
    private Map<Entity, Circle> pelletCircles = new HashMap<>(); // Séparé des joueurs

    private double mouseX = 0;
    private double mouseY = 0;
    private GameStateSnapshot currentGameState;
    private String playerName;

    private boolean isInitialized = false;

    @FXML
    public void initialize() {
        if (gamePane == null || map == null) {
            throw new IllegalStateException("FXML injection failed");
        }

        setupGamePane();
        setupBackground();
        isInitialized = true;
    }

    public void initializeNetwork(String serverAddress, int serverPort, String playerName, Stage stage) {
        if (!isInitialized) {
            throw new IllegalStateException("UI not initialized");
        }

        this.playerName = playerName;
        this.stage = stage;

        connectToServer(serverAddress, serverPort);
        setupGameLoop();
        setupMouseTracking();
    }

    private void setupGamePane() {
        gamePane.setMinSize(WIDTH, HEIGHT);
        gamePane.setPrefSize(WIDTH, HEIGHT);
        map.setPrefSize(WIDTH, HEIGHT);
    }

    private void setupBackground() {
        Image backgroundImage = new Image(getClass().getResource("/com/example/agario/quadrillage.png").toExternalForm());
        BackgroundImage bgImg = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT
        );
        gamePane.setBackground(new Background(bgImg));
        GameBorderPane.setStyle("-fx-background-color:#d8504d;");
    }

    private void connectToServer(String serverAddress, int serverPort) {
        networkExecutor.execute(() -> {
            try {
                System.out.println("Attempting to connect to server..."); // Debug 1
                socket = new Socket(serverAddress, serverPort);
                System.out.println("Socket connected!"); // Debug 2

                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                System.out.println("Streams initialized!"); // Debug 3

                oos.writeObject(playerName);
                oos.flush();
                System.out.println("Player name sent: " + playerName); // Debug 4

                // Get initial state
                System.out.println("Waiting for initial state..."); // Debug 5
                currentGameState = (GameStateSnapshot) ois.readObject();
                System.out.println("Initial state received!"); // Debug 6

                localPlayer = findPlayerByName(currentGameState.getPlayers(), playerName);
                System.out.println("Local player: " + (localPlayer != null ? localPlayer.getName() : "null")); // Debug 7

                if (localPlayer == null) {
                    return;
                }

                this.camera = new Camera(localPlayer);
                System.out.println("Starting game state update loop..."); // Debug 8

                // Game state update loop
                while (true) {
                    GameStateSnapshot newState = (GameStateSnapshot) ois.readObject();
                    System.out.printf("Received state with %d players, %d pellets%n",
                            newState.getPlayers().size(), newState.getPellets().size());

                    Platform.runLater(() -> {
                        currentGameState = newState;
                        Player updatedPlayer = findPlayerByName(currentGameState.getPlayers(), playerName);

                        if (updatedPlayer == null) {
                            System.out.println("ERROR: Player not found in update");
                            return;
                        }

                        System.out.printf("Player update: %s at (%.1f,%.1f)%n",
                                updatedPlayer.getName(), updatedPlayer.getPosX(), updatedPlayer.getPosY());

                        localPlayer = updatedPlayer;
                        updateDisplay();
                    });
                }
            } catch (Exception e) {
                System.err.println("Connection error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private Player findPlayerByName(List<Player> players, String name) {
        if (players == null || name == null) return null;

        for (Player player : players) {
            if (name.equals(player.getName())) {
                return player;
            }
        }
        return null;
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isPlayerAlive) return;

                // Calculate direction based on mouse position
                double dx = mouseX - gamePane.getWidth()/2;
                double dy = mouseY - gamePane.getHeight()/2;

                // Normalize direction
                double length = Math.sqrt(dx*dx + dy*dy);
                if (length > 0) {
                    dx /= length;
                    dy /= length;
                }

                // Send input to server
                sendPlayerInput(dx, dy);

                // Update display
                updateDisplay();
            }
        };
        gameLoop.start();
    }

    private void setupMouseTracking() {
        gamePane.setOnMouseMoved(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
        });
    }

    /*private void updateDisplay() {
        if (localPlayer == null || camera == null || currentGameState == null) return;

        System.out.printf("Rendering - Player at (%.1f,%.1f) | Zoom: %.2f | Camera: (%.1f,%.1f)%n",
                localPlayer.getPosX(),
                localPlayer.getPosY(),
                calculateZoomScale(),
                camera.getPositionX(),
                camera.getPositionY());

        // Clear previous frame
        gamePane.getChildren().clear();
        map.getChildren().clear();

        // Update camera
        camera.updateCameraDimensions();
        applyCameraTransform();

        // Get visible entities
        List<Entity> visibleEntities = getVisibleEntities();

        // Render all entities
        renderEntities(visibleEntities);

        // Update UI elements
        updateMiniMap();
        updateLeaderboard();
    }*/

    private void updateDisplay() {
        if (localPlayer == null || currentGameState == null) return;

        // Clear previous frame (but keep the background)
        gamePane.getChildren().removeIf(node -> node instanceof Circle || node instanceof Label);
        map.getChildren().clear();

        // Apply camera transform
        applyCameraTransform();

        // Render all pellets
        currentGameState.getPellets().forEach(this::renderPellet);

        // Render all players
        currentGameState.getPlayers().forEach(p -> {
            if (!p.getName().equals(localPlayer.getName())) {
                renderOtherPlayer(p);
            }
        });

        // Render local player last (on top)
        renderPlayer(localPlayer);

        updateMiniMap();
        updateLeaderboard();
    }


    private List<Entity> getVisibleEntities() {
        List<Entity> visibleEntities = new ArrayList<>();
        double scale = 1.0 / camera.getZoomFactor();
        double translateX = (gamePane.getWidth()/2) - (localPlayer.getPosX() * scale);
        double translateY = (gamePane.getHeight()/2) - (localPlayer.getPosY() * scale);

        double inverseScale = 1.0 / scale;
        Dimension cameraView = new Dimension(
                -translateX * inverseScale,
                -translateY * inverseScale,
                (-translateX + gamePane.getWidth()) * inverseScale,
                (-translateY + gamePane.getHeight()) * inverseScale
        );

        // Add visible pellets
        for (Entity pellet : currentGameState.getPellets()) {
            if (cameraView.inRange(pellet.getPosX(), pellet.getPosY())) {
                visibleEntities.add(pellet);
            }
        }

        // Add all players (visibility checked during rendering)
        visibleEntities.addAll(currentGameState.getPlayers());

        return visibleEntities;
    }

    private void renderEntities(List<Entity> entities) {
        // Render pellets first
        entities.stream()
                .filter(e -> e instanceof Pellet)
                .forEach(this::renderPellet);

        // Render local player last (on top)
        renderPlayer(localPlayer);
    }

    private void renderPellet(Entity pellet) {
        Circle circle = pelletCircles.computeIfAbsent(pellet, k -> {
            Circle c = new Circle();

            if (pellet instanceof InvisiblePellet) {
                c.setFill(Color.web("#b5dbe8"));
                c.setOpacity(0.5);
                c.setStroke(Color.BLANCHEDALMOND);
                c.setEffect(new DropShadow(10, Color.BLUE));
            }
            else if (pellet instanceof SpeedIncreasePellet) {
                c.setFill(Color.web("#ffff99"));
                c.setOpacity(0.5);
                c.setStroke(Color.YELLOW);
                c.setEffect(new DropShadow(10, Color.YELLOW));
            }
            else if (pellet instanceof SpeedDecreasePellet) {
                c.setFill(Color.web("#ff0000"));
                c.setOpacity(0.5);
                c.setStroke(Color.RED);
                c.setEffect(new DropShadow(10, Color.RED));
            }
            else {
                String color = PELLET_COLORS.get(Math.abs(pellet.hashCode()) % PELLET_COLORS.size());
                c.setFill(Color.web(color));
            }
            return c;
        });

        updateCircle(circle, pellet);
        gamePane.getChildren().add(circle);
    }

    private void renderPlayer(Player player) {
        if (player == null) return;

        boolean isLocal = player.getName().equals(localPlayer.getName());
        String color = isLocal ? PLAYER_COLOR : OTHER_PLAYER_COLOR;

        // Get or create circle
        Circle circle = playerCircles.computeIfAbsent(player.getName(), k -> {
            Circle c = new Circle();
            c.setFill(Color.web(color));
            if (isLocal) {
                c.setEffect(new DropShadow(25, Color.RED));
            }
            return c;
        });

        // Get or create label
        Label nameLabel = playerLabels.computeIfAbsent(player.getName(), k -> {
            Label l = new Label(player.getName());
            l.setTextFill(Color.WHITE);
            l.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            l.setEffect(new DropShadow(2, Color.BLACK));
            return l;
        });

        // Update positions
        updateCircle(circle, player);
        nameLabel.setLayoutX(player.getPosX() - nameLabel.getWidth()/2);
        nameLabel.setLayoutY(player.getPosY() - player.getRadius() - 10);

        // Remove old instances if they exist
        gamePane.getChildren().removeAll(circle, nameLabel);

        // Add to pane
        gamePane.getChildren().addAll(circle, nameLabel);

        if (isLocal) {
            animatePlayerMovement(circle);
        }
    }

    private void renderOtherPlayer(Player otherPlayer) {
        // Remove existing if present
        gamePane.getChildren().removeIf(node ->
                node instanceof Circle && ((Circle)node).getUserData() != null &&
                        ((Circle)node).getUserData().equals(otherPlayer.getName())
        );

        Circle circle = new Circle(
                otherPlayer.getPosX(),
                otherPlayer.getPosY(),
                otherPlayer.getRadius(),
                Color.web(OTHER_PLAYER_COLOR)
        );
        circle.setUserData(otherPlayer.getName()); // For identification
        circle.setEffect(new DropShadow(10, Color.RED));

        Label nameLabel = new Label(otherPlayer.getName());
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        nameLabel.setEffect(new DropShadow(2, Color.BLACK));
        nameLabel.setLayoutX(otherPlayer.getPosX() - nameLabel.getWidth()/2);
        nameLabel.setLayoutY(otherPlayer.getPosY() - otherPlayer.getRadius() - 10);

        gamePane.getChildren().addAll(circle, nameLabel);
    }

    private void updateCircle(Circle circle, Entity entity) {
        circle.setCenterX(entity.getPosX());
        circle.setCenterY(entity.getPosY());
        circle.setRadius(entity.getRadius());
    }

    private void animatePlayerMovement(Circle circle) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(100), circle);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }

    private void applyCameraTransform() {
        if (localPlayer == null || camera == null) return;

        // Simple follow camera - adjust these values as needed
        double scale = 0.5; // Fixed scale for now
        double translateX = gamePane.getWidth()/2 - localPlayer.getPosX() * scale;
        double translateY = gamePane.getHeight()/2 - localPlayer.getPosY() * scale;

        gamePane.getTransforms().setAll(
                new Translate(translateX, translateY),
                new Scale(scale, scale)
        );

        System.out.printf("Camera: Player at (%.1f,%.1f) | Trans: (%.1f,%.1f)%n",
                localPlayer.getPosX(), localPlayer.getPosY(),
                translateX, translateY);
    }

    private double calculateZoomScale() {
        // Zoom out as player gets bigger
        double baseScale = 0.5;
        double zoomFactor = Math.max(0.1, 50.0 / localPlayer.getRadius());
        return baseScale * zoomFactor;
    }

    private void updateMiniMap() {
        map.getChildren().clear();

        if (localPlayer == null) return;

        // Draw world boundaries
        Rectangle world = new Rectangle(0, 0, WIDTH/50, HEIGHT/50);
        world.setFill(Color.TRANSPARENT);
        world.setStroke(Color.BLUE);

        // Draw player position
        Circle playerPos = new Circle(
                localPlayer.getPosX()/50,
                localPlayer.getPosY()/50,
                3,
                Color.RED
        );

        map.getChildren().addAll(world, playerPos);
    }

    private void updateLeaderboard() {
        if (currentGameState == null) return;

        List<Player> sortedPlayers = new ArrayList<>(currentGameState.getPlayers());
        sortedPlayers.sort((p1, p2) -> Double.compare(p2.getMass(), p1.getMass()));

        List<String> leaderboardEntries = new ArrayList<>();
        for (int i = 0; i < Math.min(10, sortedPlayers.size()); i++) {
            Player p = sortedPlayers.get(i);
            leaderboardEntries.add(String.format("N°%d - %s: %.1f", i+1, p.getName(), p.getMass()));
        }

        LeaderBoardListView.getItems().setAll(leaderboardEntries);
    }

    private void sendPlayerInput(double dx, double dy) {
        if (oos != null) {
            try {
                oos.writeObject(new PlayerInput(dx, dy));
                oos.flush();
            } catch (IOException e) {
                System.err.println("Error sending input: " + e.getMessage());
            }
        }
    }


}