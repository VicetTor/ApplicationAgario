package com.example.agario.client.controllers;

import com.example.agario.client.GameClient;
import com.example.agario.models.Player;
import com.example.agario.models.factory.PlayerFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;



public class LauncherController {
    @FXML private Button OfflineGameButton;
    @FXML private Button OnlineGameButton;
    @FXML private AnchorPane LauncherAnchorPane;

    @FXML
    protected void onOfflineGameButtonClick() throws IOException {
        changeSceneOffLine();
    }

    @FXML
    protected void onOnlineGameButtonClick() throws IOException {
        changeSceneOnLine();
    }

    public void changeSceneOffLine() throws IOException {
        Stage stage = (Stage) LauncherAnchorPane.getScene().getWindow();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/agario/game.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        GameController controller = fxmlLoader.getController();

        stage.setResizable(true);
        stage.setTitle("Agar.Io");
        stage.setScene(scene);

        controller.setStage(stage);

        stage.show();

        stage.setOnCloseRequest(e -> Platform.exit());
    }

    public void changeSceneOnLine() throws IOException {
        Stage stage = (Stage) LauncherAnchorPane.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/agario/onineGame.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        OnlineGameController controller = fxmlLoader.getController();

        controller.setStage(stage);
        stage.setScene(scene);
        stage.show();

        // Lancer la connexion réseau après l'affichage de la scène
        controller.setupNetwork();
    }

}