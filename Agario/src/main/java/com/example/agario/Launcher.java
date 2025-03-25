package com.example.agario;

import com.example.agario.input.PlayerInput;
import com.example.agario.models.Player;
import com.example.agario.models.PlayerFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("launcher.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setResizable(false);
        stage.setTitle("Launcher Agar.Io");
        stage.setScene(scene);
        stage.getIcons().add(new Image("https://upload.wikimedia.org/wikipedia/commons/d/d7/Agar.io_Logo.png"));

        PlayerInput playerInput = new PlayerInput();

        scene.setOnMouseMoved(playerInput);
        PlayerFactory p = new PlayerFactory("oui");
        Player pl = (Player) p.launchFactory();

        scene.setOnMouseMoved(event ->{
            playerInput.handle(event);
            pl.setSpeed(playerInput.getMouseX(), playerInput.getMouseY(), 320, 240);
            pl.updatePosition(playerInput.getMouseX(), playerInput.getMouseY());
            System.out.println("NewX : " + pl.getPosX() + " NewY : " + pl.getPosY());
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}