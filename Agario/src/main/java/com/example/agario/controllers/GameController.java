package com.example.agario.controllers;

import com.example.agario.input.PlayerInput;
import com.example.agario.models.Entity;
import com.example.agario.models.Player;
import com.example.agario.models.PlayerFactory;
import com.example.agario.models.World;
import com.example.agario.utils.Camera;
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

        Camera cam = new Camera(player);
        List<Entity> liste = new ArrayList<>();
        World gameModel= new World(new QuadTree(1, new Dimension(0,0,getPaneWidth(),getPaneHeight())));
        gameModel.getQuadTree().DFSChunk(gameModel.getQuadTree() ,cam , liste );

        GamePane.setOnMouseMoved(playerInput);

        new Thread(()->{
            while(true){

                    player.setSpeed(playerInput.getMouseX(), playerInput.getMouseY(), 600, 600);

                    System.out.println("Player position: " + player.getPosX() + ", " + player.getPosY());

                List<Double> lst = cam.updateCameraPosition(getPaneWidth(),getPaneHeight());
                GamePane.setTranslateX(lst.get(0) );
                GamePane.setTranslateY(lst.get(1) );

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
