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

    @FXML private Pane map;
    @FXML private TextField TchatTextField;
    @FXML private Pane GamePane;
    @FXML private AnchorPane OuterPane;
    @FXML private ListView<String> LeaderBoardListView;
    @FXML private ListView<String> TchatListView;
    @FXML private GridPane gridPane;
    @FXML private BorderPane GameBorderPane;
    @FXML private Button buttonSettings;

    private Map<Entity, Circle> entitiesCircles = new HashMap<>();
    private Map<Entity, String> pelletColors = new HashMap<>();
    private HashMap<Entity, Circle> entitiesMap = new HashMap<>();
    private Game gameModel;
    private List<Player> player = new ArrayList<Player>();
    private int timer = -1;
    private Stage stage;
    private List<Double> specialSpeed = new ArrayList<Double>();
    private boolean isPlayerAlive = true;
    private boolean transitionSplit = false;


    private static final List<String> PELLET_COLORS = List.of("#ff3107", "#4e07ff", "#caff07","#ff07dc","#7107ff","#07ff2b","#07ff88","#07ff88","#07a8ff","#ff3107","#ff4f00","#ffff00");
    private static final String PLAYER_COLOR = "#7107ff";
    private static final String ROBOT_COLOR = "#07ff82";
    private static int HEIGHT = 10000;
    private static int WIDTH = 10000;
    private static int ROBOT_NUMBER = 25;
    private static int PELLET_NUMBER = 5000;
    private static String PLAYER_NAME = "Anonymous";


    private List<Player> otherPlayers = new ArrayList<>();
    private ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Socket socket;

    //private final List<Entity> visibleEntities = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupGame();
        startGameLoop();
        startPelletSpawner();
    }

    private void setupGame() {

        // Setup game pane
        GamePane.setMinSize(WIDTH, HEIGHT);
        setupBackground();

        // Initialize game model
        Dimension dimension = new Dimension(0, 0, WIDTH, HEIGHT);

        gameModel = new Game(new QuadTree(0, dimension), PLAYER_NAME, ROBOT_NUMBER);
        this.player.add(gameModel.getPlayer());
        this.specialSpeed.add(-1.0);
        gameModel.createRandomPellets(PELLET_NUMBER);
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

        LeaderBoardListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
                setTextFill(Paint.valueOf("#ffffff"));
                setBackground(Background.EMPTY);
                setStyle("-fx-background-color: transparent;");
                setPrefHeight(LeaderBoardListView.getHeight()/10.5);
            }
        });
    }

    private void startGameLoop() {
        PlayerInput playerInput = new PlayerInput();
        Camera camera = new Camera(gameModel.getPlayer());
        GamePane.setOnMouseMoved(playerInput);

        GamePane.setOnMouseClicked(event -> {

            splitPlayer(playerInput);

            System.out.println("Clic détecté aux coordonnées : X=" + event.getX() + " Y=" + event.getY());
        });

        new Thread(() -> {
            while (true) {
                //System.out.println("Timer restant: " + timer);
                timer -= 1;
               /*if (timer == 1) {

                    transitionSplit = true;

                    double averageXPlayer = 0;
                    double averageYPlayer = 0;
                    for (Player p : player){
                        averageXPlayer += p.getPosX();
                        averageYPlayer += p.getPosY();
                    }

                    averageXPlayer = averageXPlayer/player.size();
                    averageYPlayer = averageYPlayer/player.size();

                    for(Player p : player) {
                        TranslateTransition transition = new TranslateTransition(Duration.millis(500), entitiesCircles.get(p));
                        transition.setToX(averageXPlayer - entitiesCircles.get(p).getCenterX());
                        transition.setToY(averageYPlayer - entitiesCircles.get(p).getCenterY());
                        transition.play();
                    }

                }*/
                if (timer == 0) {

                    System.out.println("Fusion des joueurs!");
                    double sommeMasse = 0;
                    for (Player pl : player) {
                        sommeMasse += pl.getMass();
                    }

                    double averageXPlayer = 0;
                    double averageYPlayer = 0;
                    for (Player p : player){
                        averageXPlayer += p.getPosX();
                        averageYPlayer += p.getPosY();
                    }

                    averageXPlayer = averageXPlayer/player.size();
                    averageYPlayer = averageYPlayer/player.size();

                    player.get(0).setMass(sommeMasse);
                    player.get(0).setPosX(averageXPlayer);
                    player.get(0).setPosY(averageYPlayer);

                    int size = player.size();

                    Platform.runLater(() -> {
                        for (int i = size-1; i >=1; i--) {
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





        new Thread(() -> {
            AtomicReference<Double> dx = new AtomicReference<>(0.0);
            AtomicReference<Double> dy = new AtomicReference<>(0.0);

            while (true) {

                // Update mouse position
                for(Player p : player) {
                    if (isPlayerAlive) {
                        GamePane.setOnMouseMoved(e -> {
                            playerInput.handle(e);
                            dx.set(playerInput.getMouseX() - p.getPosX());
                            dy.set(playerInput.getMouseY() - p.getPosY());
                        });

                        p.updatePosition(dx.get(), dy.get(), GamePane.getWidth(), GamePane.getHeight());

                    }
                }
                updateRobots();

                sendPlayerUpdate();

                Platform.runLater(() -> {
                    setEntities((HashMap<Entity, Circle>) entitiesCircles);
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
            LeaderBoardListView.getItems().add(counter+". "+joueur.getName()+"     "+new DecimalFormat("0.00").format(joueur.getMass()));
            if(counter == 10) break;
        }

        LeaderBoardListView.setMinHeight(counter);

        if(gameModel.getRobots().size() == 5){
            robotSpawner(5);
        }
    }

    private void robotSpawner(int limite) {
        gameModel.createRandomRobots(limite);
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
        int i = 0;
        for(Player p : player) {
            p.setSpeed(dx, dy, stage.getHeight() / 2, stage.getWidth() / 2, specialSpeed.get(i));
            i++;
        }
        // Render all entities
        renderEntities(visibleEntities);


        otherPlayers.forEach(otherPlayer -> {
            if (!otherPlayer.getName().equals(player.get(0).getName())) {
                renderOtherPlayer(otherPlayer);
            }
        });

        // Robots absorb other entities
        for (Entity robot : new ArrayList<>(gameModel.getRobots())) {
            if (robot instanceof IA) {
                int cameraSize = 50;
                Dimension robotView = new Dimension(robot.getPosX()-cameraSize, robot.getPosY()-cameraSize,robot.getPosX()+cameraSize,robot.getPosY()+cameraSize);
                List<Entity> robotZone = new ArrayList<>();
                QuadTree.DFSChunk(gameModel.getQuadTree(), robotView, robotZone);
                robotZone.addAll(gameModel.getRobots());
                if(isPlayerAlive) {
                    for (Player p : player) {
                        robotZone.add(p);
                    }
                }
                eatEntity(robotZone, (MovableEntity) robot, gameModel.getQuadTree(), gameModel.getRobots());
            }
        }

        // Player absorbs other entities
        if(isPlayerAlive) {
            for (Player p : player) {
                eatEntity(visibleEntities, p, gameModel.getQuadTree(), gameModel.getRobots());
            }
        }

        // Update leaderboard
        updateLeaderBoard();
    }

    public void removeEntityFromHashMap(Entity entity){
        entitiesCircles.remove(entity);
    }

    public void eatPlayer(Player playerEntity){

        entitiesCircles.remove(playerEntity);
        specialSpeed.remove(playerEntity);
        player.remove(playerEntity);

        if(player.size() == 0) {
            this.isPlayerAlive = false;
            ButtonType exit = new ButtonType("Quitter", ButtonBar.ButtonData.APPLY);
            Alert alert = new Alert(Alert.AlertType.NONE, "Vous êtes mort ! Veuillez recommencer.", exit);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.APPLY){
                Platform.exit();
            }
            alert.setOnCloseRequest(e -> Platform.exit());
            Platform.exit();
        }
    }

    public void animatePelletConsumption(Entity pellet, MovableEntity p) {
        try{
            TranslateTransition transition = new TranslateTransition();
            transition.setNode(entitiesCircles.get(pellet));
            transition.setDuration(Duration.millis(50));
            transition.setToX(p.getPosX() - entitiesCircles.get(pellet).getCenterX());
            transition.setToY(p.getPosY() - entitiesCircles.get(pellet).getCenterY());
            transition.setAutoReverse(true);
            transition.setInterpolator(Interpolator.EASE_OUT);
            transition.play();
        }
        catch(NullPointerException e){
            System.out.println("Elément mangé alors qu'il n'a pas encore été instancié");
        }
    }

    private void applyCameraTransform(Camera camera) {
        double scale = 1.0 / camera.getZoomFactor();
        double screenCenterX = getPaneWidth() / 2;
        double screenCenterY = getPaneHeight() / 2;

        double averageXPlayer = 0;
        double averageYPlayer = 0;
        for (Player p : player){
            averageXPlayer += p.getPosX();
            averageYPlayer += p.getPosY();
        }

        averageXPlayer = averageXPlayer/player.size();
        averageYPlayer = averageYPlayer/player.size();

        double translateX = screenCenterX - (averageXPlayer * scale);
        double translateY = screenCenterY - (averageYPlayer * scale);

        GamePane.getTransforms().clear();
        GamePane.getTransforms().addAll(
                new Translate(translateX, translateY),
                new Scale(scale, scale, 0, 0)
        );
    }

    private List<Entity> getVisibleEntities(Camera camera) {
        List<Entity> visibleEntities = new ArrayList<>();
        double scale = 1.0 / camera.getZoomFactor();

        double averageXPlayer = 0;
        double averageYPlayer = 0;
        for (Player p : player){
            averageXPlayer += p.getPosX();
            averageYPlayer += p.getPosY();
        }

        averageXPlayer = averageXPlayer/player.size();
        averageYPlayer = averageYPlayer/player.size();

        double translateX = (getPaneWidth() / 2) - (averageXPlayer  * scale);
        double translateY = (getPaneHeight() / 2) -  (averageYPlayer  * scale);

        double inverseScale = 1.0 / scale;
        Dimension cameraView = new Dimension(
                -translateX * inverseScale,
                -translateY * inverseScale,
                (-translateX + getPaneWidth()) * inverseScale,
                (-translateY + getPaneHeight()) * inverseScale
        );

        QuadTree.DFSChunk(gameModel.getQuadTree(), cameraView, visibleEntities);
        visibleEntities.addAll(gameModel.getRobots());
        if(isPlayerAlive)
            for(Player p : player) {
                visibleEntities.add(p);
            }

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
        if(pellet instanceof InvisiblePellet){
            Circle circle = entitiesCircles.computeIfAbsent(pellet, k -> {
                Circle c = new Circle();
                String color = "#b5dbe8";
                pelletColors.put(pellet, color);
                c.setFill(Paint.valueOf(color));
                c.setOpacity(0.5);
                c.setStroke(Color.BLANCHEDALMOND);
                DropShadow glow = new DropShadow();
                glow.setColor(Color.BLUE);
                glow.setRadius(10);
                c.setEffect(glow);
                return c;
            });
            updateCircle(circle, pellet);
            GamePane.getChildren().add(circle);
        }
        else if(pellet instanceof SpeedIncreasePellet){
            Circle circle = entitiesCircles.computeIfAbsent(pellet, k -> {
                Circle c = new Circle();
                String color = "#ffff99";
                pelletColors.put(pellet, color);
                c.setFill(Paint.valueOf(color));
                c.setOpacity(0.5);
                c.setStroke(Color.YELLOW);
                DropShadow glow = new DropShadow();
                glow.setColor(Color.YELLOW);
                glow.setRadius(10);
                c.setEffect(glow);
                return c;
            });
            updateCircle(circle, pellet);
            GamePane.getChildren().add(circle);
        }
        else if(pellet instanceof SpeedDecreasePellet){
            Circle circle = entitiesCircles.computeIfAbsent(pellet, k -> {
                Circle c = new Circle();
                String color = "#ff0000";
                pelletColors.put(pellet, color);
                c.setFill(Paint.valueOf(color));
                c.setOpacity(0.5);
                c.setStroke(Color.RED);
                DropShadow glow = new DropShadow();
                glow.setColor(Color.RED);
                glow.setRadius(10);
                c.setEffect(glow);
                return c;
            });
            updateCircle(circle, pellet);
            GamePane.getChildren().add(circle);
        }
        else {
            Circle circle = entitiesCircles.computeIfAbsent(pellet, k -> {
                Circle c = new Circle();
                String color = PELLET_COLORS.get(new Random().nextInt(PELLET_COLORS.size()));
                pelletColors.put(pellet, color);
                c.setFill(Paint.valueOf(color));
                return c;
            });
            updateCircle(circle, pellet);
            GamePane.getChildren().add(circle);
        }
    }

    private void renderRobot(Entity robot) {
        Circle circle = entitiesCircles.computeIfAbsent(robot, k -> {
            Circle c = new Circle();
            c.setFill(Paint.valueOf(ROBOT_COLOR));
            return c;
        });

        updateCircle(circle, robot);
        GamePane.getChildren().add(circle);
    }

    private void renderPlayer() {
        for(Player p : player) {
            Circle circle = entitiesCircles.computeIfAbsent(p, k -> {
                Circle c = new Circle();
                c.setFill(Paint.valueOf(PLAYER_COLOR));
                return c;
            });
            Label l = new Label(p.getName());
            l.setLabelFor(circle);
            l.setTextFill(Color.WHITE);
            l.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            DropShadow shadow = new DropShadow();
            shadow.setOffsetX(1);
            shadow.setOffsetY(1);
            shadow.setColor(Color.BLACK);
            l.setEffect(shadow);

            l.widthProperty().addListener((obs, oldVal, newVal) ->
                    l.setLayoutX(circle.getCenterX() - newVal.doubleValue() / 2)
            );

            circle.centerXProperty().addListener((obs, oldVal, newVal) ->
                    l.setLayoutX(newVal.doubleValue() - l.getWidth() / 2)
            );
            circle.centerYProperty().addListener((obs, oldVal, newVal) ->
                    l.setLayoutY(newVal.doubleValue() - (l.getHeight()/2) -10)
            );

            updateCircle(circle, p);
            GamePane.getChildren().add(circle);
            GamePane.getChildren().add(l);
        }
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

        double averageXPlayer = 0;
        double averageYPlayer = 0;
        for (Player p : player){
            averageXPlayer += p.getPosX();
            averageYPlayer += p.getPosY();
        }

        averageXPlayer = averageXPlayer/player.size();
        averageYPlayer = averageYPlayer/player.size();

        double centerX = (averageXPlayer * map.getPrefWidth()) / WIDTH;
        double centerY = (averageYPlayer * map.getPrefHeight()) / HEIGHT;

        square.setX(centerX - square.getWidth() / 2);
        square.setY(centerY - square.getHeight() / 2);

        map.getChildren().add(square);

        double x1Square = averageXPlayer-1400;
        double x2Square = averageXPlayer+1400;
        double y1Square = averageYPlayer+1800;
        double y2Square = averageYPlayer-1800;

        entities.forEach((e,c) ->{

            double posXE = c.getCenterX();
            double posYE = c.getCenterY();

            if (posXE >= x1Square && posXE <= x2Square && posYE <= y1Square && posYE >= y2Square){
                Circle circle = new Circle();
                circle.setFill(c.getFill());
                circle.setCenterX((posXE * map.getPrefWidth()) / WIDTH );
                circle.setCenterY((posYE * map.getPrefHeight()) / HEIGHT);
                circle.setRadius( e.getRadius()/18 );
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

    private void renderOtherPlayer(Player otherPlayer) {
        Circle circle = entitiesCircles.computeIfAbsent(otherPlayer, k -> {
            Circle c = new Circle();
            c.setFill(Paint.valueOf("#FF0000")); // Rouge pour les autres joueurs
            return c;
        });
        updateCircle(circle, otherPlayer);
        GamePane.getChildren().add(circle);
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


    public void setPlayer(Player player){
        this.player.set(0,player);
    }

    public void invisiblePelletEffect(MovableEntity movableEntity) {
        new Thread(() -> {
            if (entitiesCircles.get(movableEntity) != null) {
                entitiesCircles.get(movableEntity).setFill(Color.TRANSPARENT);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                entitiesCircles.get(movableEntity).setFill(Paint.valueOf(PLAYER_COLOR));
            }
        }).start();
    }

    public void speedIncreaseEffect(MovableEntity movableEntity){
        new Thread(() -> {
                this.specialSpeed.set(this.player.indexOf(movableEntity),15.0);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.specialSpeed.set(this.player.indexOf(movableEntity),-1.0);
        }).start();
    }

    public void speedDecreaseEffect(MovableEntity movableEntity){
        new Thread(() -> {
            this.specialSpeed.set(this.player.indexOf(movableEntity),2.0);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.specialSpeed.set(this.player.indexOf(movableEntity),-1.0);
        }).start();
    }

    public void eatEntity(List<Entity> entities, MovableEntity movableEntity, QuadTree quadTree, List<Entity> robots) {
        List<Entity> entityToRemove = new ArrayList<>();

        for (Entity entity : entities) {
            double dx = movableEntity.getPosX() - entity.getPosX();
            double dy = movableEntity.getPosY() - entity.getPosY();
            double squareDistance = dx * dx + dy * dy;

            if (squareDistance <= movableEntity.getRadius() * (movableEntity.getRadius()*2)
                    && movableEntity.getMass() >= (entity.getMass() * 1.33)) {

                if(!(entity instanceof Player && (movableEntity.getName().equals(((Player) entity).getName())))){
                    if(entity instanceof Player && !(movableEntity.getName().equals(((Player) entity).getName()))){
                        this.eatPlayer((Player)entity);
                        break;
                    }
                    //TODO BUG ANIMATION AVEC LES PELLETS DES ROBOTS PAS A COTE DU JOUEUR
                    if (entity instanceof Pellet) {

                        if (movableEntity instanceof Player) {

                            this.animatePelletConsumption(entity, movableEntity);
                            if(entity instanceof SpeedIncreasePellet){
                                this.speedIncreaseEffect(movableEntity);
                            }
                            if(entity instanceof SpeedDecreasePellet){
                                this.speedDecreaseEffect(movableEntity);
                            }

                        }
                        if(entity instanceof InvisiblePellet){
                            this.invisiblePelletEffect(movableEntity);
                        }
                        if(entity instanceof InvisiblePellet){
                            this.invisiblePelletEffect(movableEntity);
                        }

                    }

                    // Ajouter à la liste de suppression
                    entityToRemove.add(entity);


                    // Augmenter la masse de l'entité
                    double newMass = movableEntity.getMass() + entity.getMass();
                    movableEntity.setMass(newMass);
                }



            }
        }
        // Supprimer les pellets mangés
        for (Entity entity : entityToRemove) {
            quadTree.removeNode(entity, quadTree);
            if (entity instanceof IA)
                robots.remove(entity);
            entities.remove(entity);
            this.removeEntityFromHashMap(entity);
        }
    }

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

                timer = 5;//(int) (10 + (newP.getMass()/100));

                new Thread(() -> {

                    this.specialSpeed.set(this.player.indexOf(newP),30.0);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    this.specialSpeed.set(this.player.indexOf(newP),speed);
                }).start();




            }
        }
    }

    public static int getHeight() {
        return HEIGHT;
    }

    public static int getWidth() {
        return WIDTH;
    }

    public static int getPelletNumber() {
        return PELLET_NUMBER;
    }

    public static int getRobotNumber() {
        return ROBOT_NUMBER;
    }

    public static String getPlayerName() {
        return PLAYER_NAME;
    }

    public static void setHeight(int HEIGHT) {
        GameController.HEIGHT = HEIGHT;
    }

    public static void setWidth(int WIDTH) {
        GameController.WIDTH = WIDTH;
    }

    public static void setPlayerName(String playerName) {
        PLAYER_NAME = playerName;
    }

    public static void setPelletNumber(int pelletNumber) {
        PELLET_NUMBER = pelletNumber;
    }

    public static void setRobotNumber(int robotNumber) {
        ROBOT_NUMBER = robotNumber;
    }

    @FXML
    public void openSettingsMenuClick(){
        try {
            Stage oldWindowStage = (Stage) this.GameBorderPane.getScene().getWindow();
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
}