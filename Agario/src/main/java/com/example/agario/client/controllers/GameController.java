package com.example.agario.client.controllers;

import com.example.agario.client.controllers.input.PlayerInput;
import com.example.agario.models.*;
import com.example.agario.models.factory.PlayerFactory;
import com.example.agario.models.utils.Camera;
import com.example.agario.models.utils.Dimension;
import com.example.agario.models.utils.QuadTree;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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

    private Socket gameSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private boolean isOnlineMode = false;

    private final Map<Entity, Circle> entityCircles = new HashMap<>();
    private final Map<Entity, String> pelletColors = new HashMap<>();

    private final Map<Integer, Player> otherPlayers = new HashMap<>();
    private Game gameModel;
    private Player player;
    private Stage stage;

    private static final int HEIGHT = 10000;
    private static final int WIDTH = 10000;
    private static final List<String> PELLET_COLORS = List.of("#951b8a", "#4175ba", "#12b1af");
    private static final String PLAYER_COLOR = "#251256";
    private static final String ROBOT_COLOR = "#8cb27a";



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        setupGame();
        setupBackground();

        startGameLoop();

        startPelletSpawner();


    }
    private void updateOrCreateOtherPlayer(Player serverPlayer) {
        // Si c'est notre joueur local, on ignore
        if (serverPlayer.getId() == player.getId()) {
            return;
        }

        // Vérifie si le joueur existe déjà
        Player otherPlayer = otherPlayers.get(serverPlayer.getId());

        if (otherPlayer == null) {
            // Crée un nouveau joueur
            otherPlayer = new Player(
                    serverPlayer.getPosX(),
                    serverPlayer.getPosY(),
                    serverPlayer.getName()
            );
            otherPlayer.setId(serverPlayer.getId());
            otherPlayers.put(serverPlayer.getId(), otherPlayer);
        } else {
            // Met à jour la position existante
            otherPlayer.setPosX(serverPlayer.getPosX());
            otherPlayer.setPosY(serverPlayer.getPosY());
            otherPlayer.setMass(serverPlayer.getMass());
        }
    }


    public void setNetworkConnection(Socket socket) {
        try {
            this.gameSocket = socket;
            this.isOnlineMode = true;

            // Créer d'abord le flux de sortie
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush(); // Important!

            // Puis le flux d'entrée
            this.inputStream = new ObjectInputStream(socket.getInputStream());

            // Handshake initial
            outputStream.writeObject("CLIENT_HELLO");
            outputStream.flush();

            Object response = inputStream.readObject();
            if (!"SERVER_ACK".equals(response)) {
                throw new IOException("Handshake failed");
            }

            startNetworkListener();
        } catch (Exception e) {
            System.err.println("Erreur d'initialisation: " + e.getMessage());
            Platform.runLater(this::showDisconnectionAlert);
            closeNetworkResources();
        }
    }

    private void startNetworkListener() {
        new Thread(() -> {
            try {
                InitGameData initData = (InitGameData) inputStream.readObject();
                Platform.runLater(() -> {
                    try {
                        player.setId(initData.getPlayerId());
                    } catch (Exception e) {
                        System.err.println("Erreur d'initialisation: " + e.getMessage());
                    }
                });

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        GameState gameState = (GameState) inputStream.readObject();
                        Platform.runLater(() -> {
                            try {
                                updateFromGameState(gameState);
                            } catch (Exception e) {
                                System.err.println("Erreur de mise à jour: " + e.getMessage());
                            }
                        });
                    } catch (IOException e) {
                        System.err.println("Déconnexion du serveur");
                        Platform.runLater(this::showDisconnectionAlert);
                        break;
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showDisconnectionAlert();
                    System.err.println("Erreur réseau: " + e.getMessage());
                });
            } finally {
                closeNetworkResources();
            }
        }).start();
    }

    private void closeNetworkResources() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (gameSocket != null) gameSocket.close();
        } catch (IOException e) {
            System.err.println("Erreur fermeture ressources: " + e.getMessage());
        }
    }

    private void showDisconnectionAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Connexion au serveur perdue");
        alert.showAndWait();

        // Nettoyage
        otherPlayers.clear();
        entityCircles.clear();

        Platform.exit();
    }

    private void updateFromGameState(GameState gameState) {
        // 1. Mettre à jour notre joueur local
        gameState.getPlayers().stream()
                .filter(p -> p.getId() == player.getId())
                .findFirst()
                .ifPresent(serverPlayer -> {
                    player.setPosX(serverPlayer.getPosX());
                    player.setPosY(serverPlayer.getPosY());
                    player.setMass(serverPlayer.getMass());
                });

        // 2. Mettre à jour les autres joueurs
        gameState.getPlayers().stream()
                .filter(p -> p.getId() != player.getId())
                .forEach(this::updateOrCreateOtherPlayer);

        // 3. Mettre à jour les pellets
        GamePane.getChildren().removeIf(node -> node instanceof Circle &&
                entityCircles.containsValue(node));
        entityCircles.clear();

        for (Entity pellet : gameState.getPellets()) {
            renderPellet(pellet);
        }
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
        gameModel.createRandomPellets(1000);
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
        GamePane.setBackground(new Background(bgImg));
        GameBorderPane.setStyle("-fx-background-color:#d8504d;");
    }

    private void startGameLoop() {
        PlayerInput playerInput = new PlayerInput();
        Camera camera = new Camera(player);

        GamePane.setOnMouseMoved(mouseEvent -> {
            playerInput.handle(mouseEvent);

            if (isOnlineMode && outputStream != null) { // Vérification ajoutée
                try {
                    PlayerInput networkInput = new PlayerInput(
                            mouseEvent.getX(),
                            mouseEvent.getY()
                    );
                    outputStream.writeObject(networkInput);
                    outputStream.flush();
                } catch (IOException e) {
                    System.err.println("Erreur d'envoi au serveur");
                    showDisconnectionAlert();
                }
            }
        });

        new Thread(() -> {
            AtomicReference<Double> dx = new AtomicReference<>(0.0);
            AtomicReference<Double> dy = new AtomicReference<>(0.0);

            while (true) {
                if (!isOnlineMode) {
                    // Mode offline - calcul local
                    dx.set(playerInput.getMouseX() - player.getPosX());
                    dy.set(playerInput.getMouseY() - player.getPosY());
                    player.updatePosition(dx.get(), dy.get(), GamePane.getWidth(), GamePane.getHeight());
                    updateRobots();
                }

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
        for (Entity robot : gameModel.getRobots()) {
            if (robot instanceof IA) {
                ((IA) robot).setPositionIA();
            }
        }
    }

    private void updateGameDisplay(Camera camera, double dx, double dy) {
        if (stage == null) return; // Protection contre NPE

        try {
            // Clear previous frame
            GamePane.getChildren().clear();

            // Update camera
            camera.updateCameraDimensions();

            // Apply camera transformations
            applyCameraTransform(camera);

            // Get visible entities
            List<Entity> visibleEntities = getVisibleEntities(camera);

            // Update player speed (seulement en mode offline)
            if (!isOnlineMode) {
                player.setSpeed(dx, dy, stage.getHeight()/2, stage.getWidth()/2);
            }

            // Handle collisions (seulement en mode offline)
            if (!isOnlineMode) {
                gameModel.eatPellet(visibleEntities, player);
            }

            // Render all entities
            renderEntities(visibleEntities);
        } catch (Exception e) {
            System.err.println("Erreur dans updateGameDisplay: " + e.getMessage());
        }
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
        return visibleEntities;
    }

    private void renderEntities(List<Entity> entities) {
        // Render pellets first
        entities.stream()
                .filter(e -> !(e instanceof Player) && !(e instanceof IA))
                .forEach(this::renderPellet);

        // Render robots (seulement en mode offline)
        if (!isOnlineMode) {
            entities.stream()
                    .filter(e -> e instanceof IA)
                    .forEach(this::renderRobot);
        }

        // Render other players (en mode online)
        if (isOnlineMode) {
            otherPlayers.values().forEach(this::renderOtherPlayer);
        }

        // Render main player on top
        renderPlayer();
    }

    private void renderOtherPlayer(Player otherPlayer) {
        Circle circle = entityCircles.computeIfAbsent(otherPlayer, k -> {
            Circle c = new Circle();
            c.setFill(Paint.valueOf("#FF0000")); // Rouge pour les autres joueurs
            return c;
        });

        updateCircle(circle, otherPlayer);
        GamePane.getChildren().add(circle);
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