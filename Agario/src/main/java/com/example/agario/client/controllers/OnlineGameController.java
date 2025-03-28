package com.example.agario.client.controllers;

import com.example.agario.client.GameClient;
import com.example.agario.models.*;
import com.example.agario.models.specialPellet.InvisiblePellet;
import com.example.agario.models.specialPellet.SpeedDecreasePellet;
import com.example.agario.models.specialPellet.SpeedIncreasePellet;
import com.example.agario.models.utils.Camera;
import com.example.agario.models.utils.Dimension;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
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
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

public class OnlineGameController implements Initializable {
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
    public double speed ;
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

    private String message;

    private Map<String, Circle> playerCircles = new HashMap<>(); // Suivi des cercles des joueurs
    private Map<String, Label> playerLabels = new HashMap<>();  // Suivi des labels des joueurs
    private Map<Entity, Circle> pelletCircles = new HashMap<>(); // Séparé des joueurs

    private double mouseX = 0;
    private double mouseY = 0;
    private GameStateSnapshot currentGameState;
    private String playerName;

    private boolean isInitialized = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (gamePane == null || map == null) {
            throw new IllegalStateException("FXML injection failed");
        }

        setupGamePane();
        setupBackground();
        setupChatTextField();
        isInitialized = true;
    }

    public void initializeNetwork( String playerName, Stage stage) {
        if (!isInitialized) {
            throw new IllegalStateException("UI not initialized");
        }

        this.playerName = playerName;
        this.stage = stage;

        connectToServer();
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

    private void connectToServer() {
        networkExecutor.execute(() -> {
            try {
                ConnectionResult conn = GameClient.playOnLine();
                socket = conn.socket;
                oos = conn.oos;
                ois = conn.ois;

                // Envoyer le nom
                oos.writeObject(playerName);
                oos.flush();

                // Recevoir l'état initial
                currentGameState = (GameStateSnapshot) ois.readObject();
                localPlayer = findPlayerByName(currentGameState.getPlayers(), playerName);

                if (localPlayer != null) {
                    this.camera = new Camera(localPlayer);

                }

                // Boucle de réception des mises à jour
                while (!socket.isClosed()) {
                    Object received = ois.readObject();

                    if (received instanceof GameStateSnapshot) {
                        GameStateSnapshot newState = (GameStateSnapshot) received;
                        Platform.runLater(() -> {
                            currentGameState = newState;
                            Player updatedPlayer = findPlayerByName(currentGameState.getPlayers(), playerName);
                            if (updatedPlayer != null) {
                                localPlayer = updatedPlayer;
                                if (this.camera == null) {
                                    this.camera = new Camera(localPlayer);
                                } else {
                                    this.camera.setPlayer(localPlayer);
                                }
                            }
                        });
                    }
                    else if (received instanceof ChatMessage) {
                        System.out.println("JAI RECU UN MSG DSFJHKSHDKJFSDFJHBKDSFJKHSDFQJHKBDFSHJKISDFHJKDSFJHKISDFHJKNDFSHJKSDFHJNKSDFJHKSDFJHKNDFSJHK");
                        ChatMessage chatMessage = (ChatMessage) received;
                        Platform.runLater(() -> {
                            TchatListView.getItems().add(chatMessage.getSender() + ": " + chatMessage.getMessage());
                            // Faire défiler vers le bas automatiquement
                            TchatListView.scrollTo(TchatListView.getItems().size() - 1);
                        });
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() ->
                        showErrorAlert("Erreur", "Déconnexion: " + e.getMessage())
                );
            }
        });
    }
    private void showErrorAlert(String connectionError, String message) {
        System.out.println(connectionError+message);
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

                localPlayer.setSpeed(mouseX, mouseY, stage.getHeight() / 2, stage.getWidth() / 2, specialSpeed);

                speed = localPlayer.getSpeed();



                // Send input to server
                sendPlayerInput(mouseX, mouseY);

                // Update display
                updateDisplay();
            }
        };
        gameLoop.start();
    }

    private void setupMouseTracking() {
        gamePane.setOnMouseMoved(event -> {
            // Calculate direction based on mouse position
            mouseX = event.getX() - localPlayer.getPosX();
            mouseY = event.getY() - localPlayer.getPosY();
        });
    }

    private void updateDisplay() {
        if (localPlayer == null || currentGameState == null) return;

        // Clear only player and pellet nodes
        gamePane.getChildren().removeIf(node ->
                node instanceof Circle || node instanceof Label
        );

        camera.updateCameraDimensions();
        // Update camera
        applyCameraTransform(camera);

        //System.out.println(camera.getZoomFactor()+":zoom apres Y:");

        // Render all pellets
        for (Entity pellet : currentGameState.getPellets()) {
            renderPellet(pellet);
        }

        // Render all players except local player first
        for (Player player : currentGameState.getPlayers()) {
            if (!player.getName().equals(localPlayer.getName())) {
                renderPlayer(player);
            }
        }

        // Render local player last (on top)
        renderPlayer(localPlayer);

        // Update UI
        updateMiniMap();
        updateLeaderboard();
    }





    private void renderPellet(Entity pellet) {
        Circle circle = pelletCircles.computeIfAbsent(pellet, k -> new Circle());

        // Transformer les coordonnées en teinte de couleur
        double hue = ((pellet.getPosX() + pellet.getPosY()) % 360); // Variation en fonction de X + Y
        Color pelletColor = Color.hsb(hue, 1.0, 1.0);

        circle.setFill(pelletColor);
        updateCircle(circle, pellet);
        gamePane.getChildren().add(circle);
    }

    private void renderPlayer(Player player) {
        if (player == null) {
            //System.out.println("DEBUG: Tentative de rendu d'un joueur null");
            return;
        }

        //System.out.printf("DEBUG: Rendu joueur %s à (%.1f,%.1f)\n",
               // player.getName(), player.getPosX(), player.getPosY());

        boolean isLocal = player.getName().equals(localPlayer.getName());
        String color = isLocal ? PLAYER_COLOR : OTHER_PLAYER_COLOR;

        //System.out.println("mass: "+player.getMass()+ " radius: "+player.getRadius());


        Circle circle = playerCircles.computeIfAbsent(player.getName(), k -> {
            Circle c = new Circle(player.getRadius(), Color.web(color));
            if (isLocal) {
                c.setEffect(new DropShadow(25, Color.RED));
            }
            return c;
        });

        updateCircle(circle,player);
        //System.out.println("mass: "+player.getMass()+ " radius: "+player.getRadius());

        // Gestion du label
        Label nameLabel = playerLabels.computeIfAbsent(player.getName(), k -> {
            Label l = new Label(player.getName());
            l.setTextFill(Color.WHITE);
            l.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            l.setEffect(new DropShadow(2, Color.BLACK));
            return l;
        });

        // Positionnement du label
        nameLabel.setLayoutX(player.getPosX() - nameLabel.getWidth()/2);
        nameLabel.setLayoutY(player.getPosY() - player.getRadius() - 10);

        // Nettoyage avant ajout
        gamePane.getChildren().removeAll(circle, nameLabel);
        gamePane.getChildren().addAll(circle, nameLabel);

        if (isLocal) {
            animatePlayerMovement(circle);
        }
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

    private void applyCameraTransform(Camera camera) {
        double scale = 1.0 / camera.getZoomFactor();
        double screenCenterX = gamePane.getWidth() / 2;
        double screenCenterY = gamePane.getHeight() / 2;

        double translateX = screenCenterX - (localPlayer.getPosX() * scale);
        double translateY = screenCenterY - (localPlayer.getPosY() * scale);

        gamePane.getTransforms().clear();
        gamePane.getTransforms().addAll(
                new Translate(translateX, translateY),
                new Scale(scale, scale, 0, 0)
        );
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

    private void setupChatTextField() {
        TchatTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String message = TchatTextField.getText().trim();
                if (!message.isEmpty()) {
                    try {
                        // Créer et envoyer le message chat
                        ChatMessage chatMessage = new ChatMessage(playerName, message);
                        oos.writeObject(chatMessage);
                        oos.flush();

                        // Ajouter le message localement
                        TchatListView.getItems().add("Vous: " + message);
                        TchatListView.scrollTo(TchatListView.getItems().size() - 1);

                        // Vider le champ de texte
                        TchatTextField.clear();
                    } catch (IOException e) {
                        System.err.println("Erreur lors de l'envoi du message: " + e.getMessage());
                    }
                }
                event.consume();
            }
        });
    }

    private void sendPlayerInput(double dx, double dy) {
        if (oos != null) {
            try {
                oos.writeObject(new PlayerInput(dx, dy,speed));
                oos.flush();
            } catch (IOException e) {
                System.err.println("Error sending input: " + e.getMessage());
            }
        }
    }
}