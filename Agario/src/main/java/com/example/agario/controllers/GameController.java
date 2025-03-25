package com.example.agario.controllers;

import com.example.agario.input.PlayerInput;
import com.example.agario.models.Entity;
import com.example.agario.models.Game;
import com.example.agario.models.Player;
import com.example.agario.models.PlayerFactory;
import com.example.agario.utils.Dimension;
import com.example.agario.utils.QuadTree;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    @FXML private TextField TchatTextField;
    @FXML private Pane GamePane;
    @FXML private ListView LeaderBoardListView;
    @FXML private ListView TchatListView;

    private Game gameModel;

    //TEMPORAIRE (manque camera)
    private Dimension dimension;
    private Player player;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialisation");

        dimension = new Dimension(0, 0, 2000, 2000);

        gameModel = new Game(new QuadTree(0, dimension));
        gameModel.createRandomPellets();

        displayPellets();

        this.player = (Player) new PlayerFactory("Pourquoi pas ?").launchFactory();
        Circle playerCircle = new Circle();
        playerCircle.setFill(Paint.valueOf("#251256"));
        playerCircle.centerXProperty().bindBidirectional(player.getPosXProperty());
        playerCircle.centerYProperty().bindBidirectional(player.getPosYProperty());
        playerCircle.radiusProperty().bindBidirectional(player.getRadiusProperty());

        PlayerInput playerInput = new PlayerInput();

        GamePane.setOnMouseMoved(playerInput);

        new Thread(()->{
            while(true){
                player.updatePosition(playerInput.getMouseX(), playerInput.getMouseY());
                System.out.println("NewX : " + player.getPosX() + " NewY : " + player.getPosY());
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        GamePane.setOnMouseMoved(event ->{
            playerInput.handle(event);
            player.setSpeed(playerInput.getMouseX(), playerInput.getMouseY(), 600, 600);
            System.out.println("Mouse moved : " + player.getPosX() + " Y : " + player.getPosY());
        });

        gameModel.getQuadTree().insertNode(player);

        GamePane.getChildren().add(playerCircle);
    }

    public double getPaneWidth(){
        return GamePane.getBoundsInParent().getWidth();
    }

    public double getPaneHeight(){return GamePane.getBoundsInParent().getHeight(); }

    public void displayPellets(){
        ArrayList<Entity> liste = new ArrayList<>();
        QuadTree.DFSChunk(gameModel.getQuadTree(), dimension, liste);

        for(Entity pellet : liste){
            Circle pelletCircle = new Circle();

            List<String> colors = new ArrayList<>();
            colors.add("#951b8a");colors.add("#4175ba");colors.add("#12b1af");

            pelletCircle.setFill(Paint.valueOf(colors.get(new Random().nextInt(3))));
            pelletCircle.centerXProperty().bind(pellet.getPosXProperty());
            pelletCircle.centerYProperty().bind(pellet.getPosYProperty());
            pelletCircle.radiusProperty().bind(pellet.getRadiusProperty());

            GamePane.getChildren().add(pelletCircle);
        }
    }

}
