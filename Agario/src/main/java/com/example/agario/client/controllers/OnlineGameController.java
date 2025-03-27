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
    @FXML private Pane GamePane;
    @FXML private AnchorPane OuterPane;
    @FXML private ListView<String> LeaderBoardListView;
    @FXML private ListView<String> TchatListView;
    @FXML private GridPane gridPane;
    @FXML private BorderPane GameBorderPane;

    private Map<Entity, Circle> entitiesCircles = new HashMap<>();
    private Map<Entity, String> pelletColors = new HashMap<>();
    private HashMap<Entity, Circle> entitiesMap = new HashMap<>();
    private List<Player> player = new ArrayList<>();
    private List<Player> otherPlayers = new ArrayList<>();

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

    private Player localPlayer; // Référence au joueur local
    private List<Player> allPlayers = new ArrayList<>();

    private double mouseX = 0;
    private double mouseY = 0;
    private GameStateSnapshot currentGameState;
    private String playerName;

    public void initialize(String serverAddress, int serverPort, String playerName, Stage stage) {
        this.playerName = playerName;
        this.stage = stage;

        setupGamePane();
        setupBackground();
        connectToServer(serverAddress, serverPort);
        setupGameLoop();
        setupMouseTracking();
    }

    private void setupGamePane() {
        GamePane.setMinSize(WIDTH, HEIGHT);
        GamePane.setPrefSize(WIDTH, HEIGHT);
        map.setPrefSize(WIDTH, HEIGHT);
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

    private void connectToServer(String serverAddress, int serverPort) {
        networkExecutor.execute(() -> {
            try {
                socket = new Socket(serverAddress, serverPort);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                // Envoyer le nom du joueur
                oos.writeObject(playerName);
                oos.flush();

                // Recevoir l'état initial
                currentGameState = (GameStateSnapshot) ois.readObject();
                Player initialPlayer = findPlayerByName(currentGameState.getPlayers(), playerName);

                if (initialPlayer == null) {
                    Platform.runLater(() -> showErrorAndExit("Failed to initialize player"));
                    return;
                }

                this.player.add(initialPlayer);
                this.camera = new Camera(initialPlayer);

                // Recevoir les mises à jour du serveur
                while (true) {
                    GameStateSnapshot newState = (GameStateSnapshot) ois.readObject();
                    Platform.runLater(() -> {
                        currentGameState = newState;
                        Player updatedPlayer = findPlayerByName(currentGameState.getPlayers(), playerName);
                        if (updatedPlayer == null) {
                            isPlayerAlive = false;
                            showGameOver();
                        } else {
                            player.set(0, updatedPlayer);
                            updateDisplay();
                        }
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> showErrorAndExit("Connection error: " + e.getMessage()));
            }
        });
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isPlayerAlive && !player.isEmpty()) {
                    // Calculer la direction basée sur la position de la souris
                    double dx = mouseX - GamePane.getWidth() / 2;
                    double dy = mouseY - GamePane.getHeight() / 2;

                    // Envoyer l'input au serveur
                    sendPlayerInput(dx, dy);

                    // Mettre à jour le leaderboard
                    updateLeaderboard();
                }
            }
        };
        gameLoop.start();
    }

    private void handleGameStateUpdate(GameStateSnapshot newState) {
        // Mettre à jour la référence au joueur local
        Player updatedLocalPlayer = findPlayerByName(newState.getPlayers(), playerName);
        if (updatedLocalPlayer == null) {
            isPlayerAlive = false;
            showGameOver();
            return;
        }

        localPlayer = updatedLocalPlayer;
        allPlayers = new ArrayList<>(newState.getPlayers());

        // Nettoyer les joueurs disparus
        Set<String> currentPlayerNames = new HashSet<>();
        newState.getPlayers().forEach(p -> currentPlayerNames.add(p.getName()));

        playerCircles.keySet().removeIf(name -> !currentPlayerNames.contains(name));
        playerLabels.keySet().removeIf(name -> !currentPlayerNames.contains(name));

        Platform.runLater(this::updateDisplay);
    }

    private void setupMouseTracking() {
        GamePane.setOnMouseMoved(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
        });
    }

    private void updateDisplay() {
        if (localPlayer == null || camera == null) return;

        // Mise à jour de la caméra basée sur le joueur local
        camera.updateCameraDimensions();
        applyCameraTransform(camera);

        // Nettoyer l'affichage
        GamePane.getChildren().clear();
        map.getChildren().clear();

        // Récupérer les entités visibles
        List<Entity> visibleEntities = getVisibleEntities(camera);

        // Afficher les pellets
        visibleEntities.stream()
                .filter(e -> e instanceof Pellet)
                .forEach(this::renderPellet);

        // Afficher tous les joueurs (y compris le local)
        visibleEntities.stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player)e)
                .sorted(Comparator.comparingDouble(p ->
                        p.getName().equals(localPlayer.getName()) ? 1 : 0)) // Joueur local en dernier
                .forEach(this::renderPlayer);

        updateMiniMap();
        updateLeaderboard();
    }

    private List<Entity> getVisibleEntities(Camera camera) {
        List<Entity> visibleEntities = new ArrayList<>();
        double scale = 1.0 / camera.getZoomFactor();
        double translateX = (GamePane.getWidth() / 2) - (player.get(0).getPosX() * scale);
        double translateY = (GamePane.getHeight() / 2) - (player.get(0).getPosY() * scale);

        double inverseScale = 1.0 / scale;
        Dimension cameraView = new Dimension(
                -translateX * inverseScale,
                -translateY * inverseScale,
                (-translateX + GamePane.getWidth()) * inverseScale,
                (-translateY + GamePane.getHeight()) * inverseScale
        );

        // Ajouter les pellets visibles
        for (Entity pellet : currentGameState.getPellets()) {
            if (cameraView.inRange(pellet.getPosX(), pellet.getPosY())) {
                visibleEntities.add(pellet);
            }
        }

        // Ajouter les joueurs visibles
        visibleEntities.addAll(currentGameState.getPlayers());

        return visibleEntities;
    }

    private void renderEntities(List<Entity> entities) {
        /*
        // Afficher les pellets en premier
        entities.stream()
                .filter(e -> e instanceof Pellet)
                .forEach(this::renderPellet);

        // Afficher les autres joueurs
        entities.stream()
                .filter(e -> e instanceof Player && !((Player)e).getName().equals(playerName))
                .forEach(this::renderOtherPlayer);

        // Afficher le joueur local par dessus
        if (isPlayerAlive && !player.isEmpty()) {
            renderPlayer();
        }
        */

    }

    private void renderPellet(Entity pellet) {
        Circle circle = entitiesCircles.computeIfAbsent(pellet, k -> {
            Circle c = new Circle();

            if (pellet instanceof InvisiblePellet) {
                c.setFill(Paint.valueOf("#b5dbe8"));
                c.setOpacity(0.5);
                c.setStroke(Color.BLANCHEDALMOND);
                DropShadow glow = new DropShadow();
                glow.setColor(Color.BLUE);
                glow.setRadius(10);
                c.setEffect(glow);
            }
            else if (pellet instanceof SpeedIncreasePellet) {
                c.setFill(Paint.valueOf("#ffff99"));
                c.setOpacity(0.5);
                c.setStroke(Color.YELLOW);
                DropShadow glow = new DropShadow();
                glow.setColor(Color.YELLOW);
                glow.setRadius(10);
                c.setEffect(glow);
            }
            else if (pellet instanceof SpeedDecreasePellet) {
                c.setFill(Paint.valueOf("#ff0000"));
                c.setOpacity(0.5);
                c.setStroke(Color.RED);
                DropShadow glow = new DropShadow();
                glow.setColor(Color.RED);
                glow.setRadius(10);
                c.setEffect(glow);
            }
            else {
                String color = PELLET_COLORS.get(Math.abs(pellet.hashCode()) % PELLET_COLORS.size());
                c.setFill(Paint.valueOf(color));
                pelletColors.put(pellet, color);
            }
            return c;
        });

        updateCircle(circle, pellet);
        GamePane.getChildren().add(circle);
    }

    private void renderPlayer(Player player) {
        boolean isLocalPlayer = player.getName().equals(localPlayer.getName());
        String playerColor = isLocalPlayer ? PLAYER_COLOR : OTHER_PLAYER_COLOR;

        Circle circle = playerCircles.computeIfAbsent(player.getName(), k -> {
            Circle c = new Circle();
            c.setFill(Paint.valueOf(playerColor));

            // Ajouter un effet pour les autres joueurs
            if (!isLocalPlayer) {
                DropShadow glow = new DropShadow();
                glow.setColor(Color.RED);
                glow.setRadius(10);
                c.setEffect(glow);
            }
            return c;
        });

        Label label = playerLabels.computeIfAbsent(player.getName(), k -> {
            Label l = new Label(player.getName());
            l.setTextFill(Color.WHITE);
            l.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            DropShadow shadow = new DropShadow();
            shadow.setOffsetX(1);
            shadow.setOffsetY(1);
            shadow.setColor(Color.BLACK);
            l.setEffect(shadow);

            return l;
        });

        // Mise à jour position et taille
        circle.setCenterX(player.getPosX());
        circle.setCenterY(player.getPosY());
        circle.setRadius(player.getRadius());

        // Positionnement du label
        label.setLayoutX(player.getPosX() - label.getWidth()/2);
        label.setLayoutY(player.getPosY() - player.getRadius() - 10);

        // Ajouter au GamePane si ce n'est pas déjà fait
        if (!GamePane.getChildren().contains(circle)) {
            GamePane.getChildren().add(circle);
        }
        if (!GamePane.getChildren().contains(label)) {
            GamePane.getChildren().add(label);
        }

        // Animation pour le joueur local
        if (isLocalPlayer) {
            animatePlayerMovement(circle);
        }
    }

    private void renderOtherPlayer(Player otherPlayer) {
        Circle circle = entitiesCircles.computeIfAbsent(otherPlayer, k -> {
            Circle c = new Circle();
            c.setFill(Paint.valueOf(OTHER_PLAYER_COLOR));
            return c;
        });

        Label nameLabel = new Label(otherPlayer.getName());
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(1);
        shadow.setOffsetY(1);
        shadow.setColor(Color.BLACK);
        nameLabel.setEffect(shadow);

        nameLabel.layoutXProperty().bind(circle.centerXProperty().subtract(nameLabel.widthProperty().divide(2)));
        nameLabel.layoutYProperty().bind(circle.centerYProperty().subtract(circle.radiusProperty().add(10)));

        updateCircle(circle, otherPlayer);
        GamePane.getChildren().addAll(circle, nameLabel);
    }

    private void animatePlayerMovement(Circle playerCircle) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), playerCircle);
        scaleTransition.setToX(1.05);
        scaleTransition.setToY(1.05);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);
        scaleTransition.play();
    }

    private void updateCircle(Circle circle, Entity entity) {
        circle.setCenterX(entity.getPosX());
        circle.setCenterY(entity.getPosY());
        circle.setRadius(entity.getRadius());
    }

    private void applyCameraTransform(Camera camera) {
        double scale = 1.0 / camera.getZoomFactor();
        double screenCenterX = GamePane.getWidth() / 2;
        double screenCenterY = GamePane.getHeight() / 2;

        double translateX = screenCenterX - (player.get(0).getPosX() * scale);
        double translateY = screenCenterY - (player.get(0).getPosY() * scale);

        GamePane.getTransforms().clear();
        GamePane.getTransforms().addAll(
                new Translate(translateX, translateY),
                new Scale(scale, scale, 0, 0)
        );
    }

    private void updateMiniMap() {
        map.getChildren().clear();

        if (player.isEmpty()) return;

        // Indicateur du joueur
        Rectangle playerIndicator = new Rectangle(50, 50);
        playerIndicator.setFill(null);
        playerIndicator.setStroke(Color.RED);
        playerIndicator.setStrokeWidth(1);

        double centerX = (player.get(0).getPosX() * map.getPrefWidth()) / WIDTH;
        double centerY = (player.get(0).getPosY() * map.getPrefHeight()) / HEIGHT;

        playerIndicator.setX(centerX - playerIndicator.getWidth() / 2);
        playerIndicator.setY(centerY - playerIndicator.getHeight() / 2);

        map.getChildren().add(playerIndicator);

        // Zone visible
        double viewWidth = 2800 * map.getPrefWidth() / WIDTH;
        double viewHeight = 3600 * map.getPrefHeight() / HEIGHT;

        Rectangle viewArea = new Rectangle(viewWidth, viewHeight);
        viewArea.setFill(null);
        viewArea.setStroke(Color.BLUE);
        viewArea.setStrokeWidth(1);
        viewArea.setX(centerX - viewWidth / 2);
        viewArea.setY(centerY - viewHeight / 2);

        map.getChildren().add(viewArea);

        // Autres entités
        entitiesCircles.forEach((entity, circle) -> {
            if (entity instanceof Player) {
                double posX = (entity.getPosX() * map.getPrefWidth()) / WIDTH;
                double posY = (entity.getPosY() * map.getPrefHeight()) / HEIGHT;

                Circle miniCircle = new Circle();
                miniCircle.setFill(circle.getFill());
                miniCircle.setCenterX(posX);
                miniCircle.setCenterY(posY);
                miniCircle.setRadius(entity.getRadius() / 18);

                map.getChildren().add(miniCircle);
            }
        });
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
                PlayerInput input = new PlayerInput(dx, dy);
                oos.writeObject(input);
                oos.flush();
            } catch (IOException e) {
                System.err.println("Error sending input: " + e.getMessage());
            }
        }
    }

    private Player findPlayerByName(List<Player> players, String name) {
        return players.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private void showGameOver() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Vous êtes mort ! Veuillez recommencer.");

        ButtonType exitButton = new ButtonType("Quitter", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(exitButton);

        alert.showAndWait();
        shutdown();
    }

    private void showErrorAndExit(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(message);
        alert.showAndWait();
        shutdown();
    }

    public void shutdown() {
        try {
            if (oos != null) oos.close();
            if (ois != null) ois.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error shutting down: " + e.getMessage());
        }
        networkExecutor.shutdown();
        if (gameLoop != null) gameLoop.stop();
        Platform.runLater(() -> stage.close());
    }

    @FXML
    private void sendChatMessage() {
        String message = TchatTextField.getText();
        if (!message.isEmpty() && oos != null) {
            try {
                oos.writeObject(message);
                oos.flush();
                TchatTextField.clear();
            } catch (IOException e) {
                System.err.println("Error sending chat message: " + e.getMessage());
            }
        }
    }
}