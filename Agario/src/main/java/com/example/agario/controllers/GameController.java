package com.example.agario.controllers;

import com.example.agario.models.Entity;
import com.example.agario.models.Game;
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

    private Entity player;
    private Game gameModel;

    //TEMPORAIRE (manque camera)
    private double WIDTH;
    private double HEIGHT;
    private Dimension dimension;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialisation");

        WIDTH = getPaneWidth();
        HEIGHT = getPaneHeight();
        dimension = new Dimension(0, 0, 2000, 2000);

        gameModel = new Game(new QuadTree(0, dimension));
        gameModel.createRandomPellets();

        displayPellets();

        this.player = new PlayerFactory("Pourquoi pas ?").launchFactory();
        Circle playerCircle = new Circle();
        playerCircle.setFill(Paint.valueOf("#251256"));
        playerCircle.centerXProperty().bind(player.getPosXProperty());
        playerCircle.centerYProperty().bind(player.getPosYProperty());
        playerCircle.radiusProperty().bind(player.getRadiusProperty());

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
