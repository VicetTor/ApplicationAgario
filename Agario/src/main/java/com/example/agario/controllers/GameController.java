package com.example.agario.controllers;

import com.example.agario.input.PlayerInput;
import com.example.agario.models.Entity;
import com.example.agario.models.Player;
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

    private Player player;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialisation");
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

        GamePane.getChildren().add(playerCircle);
    }

    public double getPaneWidth(){
        return GamePane.getBoundsInParent().getWidth();
    }

    public double getPaneHeight(){
        return GamePane.getBoundsInParent().getHeight();
    }

}
