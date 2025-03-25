package com.example.agario.controllers;

import com.example.agario.models.Entity;
import com.example.agario.models.PlayerFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    @FXML private TextField TchatTextField;
    @FXML private Pane GamePane;
    @FXML private ListView LeaderBoardListView;
    @FXML private ListView TchatListView;

    private Entity player;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialisation");
        this.player = new PlayerFactory("Pourquoi pas ?").launchFactory();
        Circle playerCircle = new Circle();
        playerCircle.setFill(Paint.valueOf("#251256"));
        playerCircle.centerXProperty().bind(player.getPosXProperty());
        playerCircle.centerYProperty().bind(player.getPosYProperty());
        playerCircle.radiusProperty().bind(player.getRadiusProperty());

        GamePane.getChildren().add(playerCircle);
    }

    public double getPaneWidth(){
        return GamePane.getBoundsInParent().getWidth();
    }

    public double getPaneHeight(){
        return GamePane.getBoundsInParent().getHeight();
    }

}
