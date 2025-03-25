package com.example.agario;

import com.example.agario.input.PlayerInput;
import com.example.agario.models.Player;
import com.example.agario.models.PlayerFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("launcher.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 600);

        stage.setResizable(false);
        stage.setTitle("Launcher Agar.Io");
        stage.setScene(scene);
        stage.getIcons().add(new Image("https://upload.wikimedia.org/wikipedia/commons/d/d7/Agar.io_Logo.png"));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}