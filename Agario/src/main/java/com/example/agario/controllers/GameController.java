package com.example.agario.controllers;

import com.example.agario.input.PlayerInput;
import com.example.agario.models.Player;
import com.example.agario.models.PlayerFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    @FXML private TextField TchatTextField;
    @FXML private Pane GamePane;
    @FXML private ListView LeaderBoardListView;
    @FXML private ListView TchatListView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialisation");
    }

    public double getPaneWidth(){
        return GamePane.getBoundsInParent().getWidth();
    }

    public double getPaneHeight(){
        return GamePane.getBoundsInParent().getHeight();
    }


}
