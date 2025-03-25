package com.example.agario.controllers;

import com.example.agario.controllers.GameController;
import com.example.agario.input.PlayerInput;
import com.example.agario.models.Player;
import com.example.agario.models.PlayerFactory;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class LauncherController {
    @FXML private Button OfflineGameButton;
    @FXML private Button OnlineGameButton;
    @FXML private AnchorPane LauncherAnchorPane;

    @FXML
    protected void onOfflineGameButtonClick() throws IOException {
        changeScene();
    }

    @FXML
    protected void onOnlineGameButtonClick() throws IOException {
        changeScene();
    }

    public void changeScene() throws IOException {
        Stage stage = (Stage) LauncherAnchorPane.getScene().getWindow();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/agario/game.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        GameController controller = fxmlLoader.getController();

        stage.setResizable(true);
        stage.setTitle("Agar.Io");
        stage.setScene(scene);
        stage.show();
    }
}