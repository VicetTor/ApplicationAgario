package com.example.agario.client.controllers;

import com.example.agario.client.GameClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
        //controller.ecoute();


        stage.show();
        stage.setOnCloseRequest(e -> Platform.exit());
    }

    public void changeSceneOnLine() throws IOException {
        Socket socket = GameClient.playOnLine();
        if (socket == null) {
            System.err.println("Erreur de connexion");
            return;
        }

        Stage stage = (Stage) LauncherAnchorPane.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/agario/game.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        GameController controller = fxmlLoader.getController();

        // Configuration avant d'afficher
        controller.setStage(stage); // IMPORTANT: Setter la scène d'abord
        controller.setNetworkConnection(socket);

        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            GameClient.closeConnection(socket);
            Platform.exit();
        });
        stage.show();
    }

}