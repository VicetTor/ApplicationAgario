package com.example.agario.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LauncherController {
    @FXML private Button OfflineGameButton;
    @FXML private Button OnlineGameButton;
    @FXML private AnchorPane LauncherAnchorPane;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private Alert loadingAlert;

    @FXML
    private void initialize() {
        // Initialisation des styles ou autres configurations si nécessaire
    }

    @FXML
    protected void onOfflineGameButtonClick() {
        launchOfflineGame();
    }

    @FXML
    protected void onOnlineGameButtonClick() {
        requestPlayerName().ifPresent(this::handleOnlineGameConnection);
    }

    private Optional<String> requestPlayerName() {
        TextInputDialog dialog = new TextInputDialog("Player" + (int)(Math.random() * 1000));
        dialog.setTitle("Connexion au serveur");
        dialog.setHeaderText("Entrez votre pseudo");
        dialog.setContentText("Pseudo:");
        return dialog.showAndWait();
    }

    private void handleOnlineGameConnection(String playerName) {
        showLoadingAlert("Connexion au serveur en cours...");

        CompletableFuture.supplyAsync(() -> testServerConnection())
                .thenAccept(isConnected -> Platform.runLater(() -> {
                    if (isConnected) {
                        closeLoadingAlert();
                        launchOnlineGame(playerName);
                    } else {
                        closeLoadingAlert();
                        showConnectionError();
                    }
                }));
    }

    private boolean testServerConnection() {
        try (Socket testSocket = new Socket(SERVER_HOST, SERVER_PORT)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void launchOfflineGame() {
        try {
            Stage stage = (Stage) LauncherAnchorPane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/agario/game.fxml"));
            Scene scene = new Scene(loader.load());

            GameController controller = loader.getController();
            controller.setStage(stage);

            configureStage(stage, scene, "Agar.Io - Mode Hors-ligne");
        } catch (IOException e) {
            showErrorAlert("Erreur de chargement", "Impossible de charger le mode hors-ligne");
        }
    }

    private void launchOnlineGame(String playerName) {
        try {
            Stage stage = (Stage) LauncherAnchorPane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/agario/onlineGame.fxml"));

            // Load FIRST
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Get controller AFTER loading
            OnlineGameController controller = loader.getController();

            // Initialize network SECOND
            Platform.runLater(() -> {
                try {
                    controller.initializeNetwork( playerName, stage);
                    configureStage(stage, scene, "Agar.Io - Online Mode (" + playerName + ")");
                } catch (Exception e) {
                    showErrorAlert("Initialization Error", e.getMessage());
                }
            });
        } catch (IOException e) {
            showErrorAlert("Loading Error", "Could not load online game UI");
        }
    }

    private void configureStage(Stage stage, Scene scene, String title) {
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setResizable(true);
        stage.show();

        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }

    private void showLoadingAlert(String message) {
        loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Connexion");
        loadingAlert.setHeaderText(message);
        loadingAlert.initOwner(LauncherAnchorPane.getScene().getWindow());

        // Empêcher la fermeture par l'utilisateur
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        loadingAlert.getButtonTypes().setAll(cancelButton);

        loadingAlert.show();
    }

    private void closeLoadingAlert() {
        if (loadingAlert != null && loadingAlert.isShowing()) {
            loadingAlert.close();
        }
    }

    private void showConnectionError() {
        showErrorAlert(
                "Erreur de connexion",
                "Impossible de se connecter au serveur.\nVérifiez que le serveur est lancé et réessayez."
        );
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(LauncherAnchorPane.getScene().getWindow());
        alert.showAndWait();
    }
}