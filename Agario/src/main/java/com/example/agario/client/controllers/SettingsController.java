package com.example.agario.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class SettingsController implements Initializable {

    @FXML private Button quitButton;
    @FXML private Button applyButton;

    @FXML private Label namePlayerLabel;
    @FXML private TextField namePlayerTextField;
    @FXML private Label pelletsLabel;
    @FXML private TextField pelletsTextField;
    @FXML private Label robotsLabel;
    @FXML private TextField robotsTextField;
    @FXML private Label widthMapLabel;
    @FXML private TextField widthMapTextField;
    @FXML private Label heightMapLabel;
    @FXML private TextField heightMapTextField;

    private String errorMessage;
    private int counterError;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.errorMessage = "Données invalides : \n";
        this.counterError = 0;
        namePlayerTextField.setText(GameController.getPlayerName());
        pelletsTextField.setText(GameController.getPelletNumber()+"");
        robotsTextField.setText(GameController.getRobotNumber()+"");
        widthMapTextField.setText(GameController.getWidth()+"");
        heightMapTextField.setText(GameController.getHeight()+"");
    }

    /**
     * quit the settings menu and relaunch the game
     */
    @FXML
    public void quitButtonClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/com/example/agario/game.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            Stage stage = new Stage();
            GameController controller = fxmlLoader.getController();
            stage.setResizable(true);
            stage.setTitle("Agar.Io");
            stage.setScene(scene);

            controller.setStage(stage);

            stage.show();

            stage.setOnCloseRequest(e -> Platform.exit());

            Stage oldStage = (Stage) quitButton.getScene().getWindow();
            oldStage.close();
        } catch (IOException e) {
            System.out.println("ERREUR");
        }
    }

    /**
     * apply the modifications if valid
     */
    @FXML
    public void applyButtonClick(){
        List<Boolean> validator = new ArrayList<>();
        List<String> errorTexts = List.of(
                "Nom : Il faut au moins 1 max 30 caractères.\n",
                "Pastilles : Il faut un nombre qui ne dépasse pas 7000.\n",
                "Robots : Il faut un nombre qui ne dépasse pas 50.\n",
                "Largeur : Il faut un nombre min de 600 max de 100000.\n",
                "Hauteur : Il faut un nombre min de 600 max de 100000.\n");

        validator.add(namePlayerTextField.getText().matches("^.{1,30}$"));
        validator.add(pelletsTextField.getText().matches("^(?:[0-9]|[1-9][0-9]{1,2}|[1-6][0-9]{3}|7000)$"));
        validator.add(robotsTextField.getText().matches("^(?:[0-9]|[1-4][0-9]|50)$"));
        validator.add(widthMapTextField.getText().matches("^(?:[6-9][0-9]{2}|[1-9][0-9]{3,4}|100000)$"));
        validator.add(heightMapTextField.getText().matches("^(?:[6-9][0-9]{2}|[1-9][0-9]{3,4}|100000)$"));

        for(int i = 0; i < validator.size(); i++){
            if(!validator.get(i)){
                errorMessage += errorTexts.get(i);
                counterError++;
            }
        }

        if(counterError == 0){
            GameController.setPlayerName(namePlayerTextField.getText());
            GameController.setPelletNumber(Integer.parseInt(pelletsTextField.getText()));
            GameController.setRobotNumber(Integer.parseInt(robotsTextField.getText()));
            GameController.setWidth(Integer.parseInt(widthMapTextField.getText()));
            GameController.setHeight(Integer.parseInt(heightMapTextField.getText()));
        }
        else {
            ButtonType buttonOk = new ButtonType("OK", ButtonBar.ButtonData.APPLY);
            Alert alert = new Alert(Alert.AlertType.NONE, errorMessage, buttonOk);
            Optional<ButtonType> result = alert.showAndWait();
            counterError = 0;
            errorMessage = "Données invalides : \n";
            if (result.get() == ButtonType.APPLY){
                alert.close();
            }
        }
    }
}
