package com.example.agario.controllers;

import com.example.agario.input.PlayerInput;
import com.example.agario.models.Entity;
import com.example.agario.models.Player;
import com.example.agario.models.PlayerFactory;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    @FXML
    private TextField TchatTextField;
    @FXML
    private Pane GamePane;
    @FXML
    private ListView LeaderBoardListView;
    @FXML
    private ListView TchatListView;

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


        GamePane.getChildren().add(playerCircle);
    }

    public void ecoute() {
        PlayerInput playerInput = new PlayerInput();

        GamePane.setOnMouseMoved(playerInput);

        GamePane.setOnMouseMoved(event -> {
            playerInput.handle(event);
            player.setSpeed(playerInput.getMouseX(), playerInput.getMouseY());
            System.out.println("Mouse moved : " + player.getPosX() + " Y : " + player.getPosY());
        });

        new Thread(() -> {
            playerInput.setMouseX(getPaneWidth()/2);
            playerInput.setMouseY(getPaneHeight()/2);
            while (true) {
                player.updatePosition(playerInput.getMouseX(), playerInput.getMouseY());
                System.out.println(playerInput.getMouseX());
                System.out.println("Width" + getPaneWidth());
                // System.out.println("NewX : " + player.getPosX() + " NewY : " + player.getPosY());
                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public double getPaneWidth() {
        return GamePane.getBoundsInParent().getWidth();
    }

    public double getPaneHeight() {
        return GamePane.getBoundsInParent().getHeight();
    }

    /*public static void delay(long millis, Runnable continuation) {
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try { Thread.sleep(millis); }
                catch (InterruptedException e) { }
                return null;
            }
        };
        sleeper.setOnSucceeded(event -> continuation.run());
        new Thread(sleeper).start();
    }*/

}
