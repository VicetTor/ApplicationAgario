package com.example.agario.input;

import com.example.agario.models.Player;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class PlayerInput implements EventHandler<MouseEvent> {

    double mouseX;
    double mouseY;

    Player player;

    public PlayerInput(Player player){
        this.player = player;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        mouseX = mouseEvent.getX();
        mouseY = mouseEvent.getY();

        System.out.println("Mouse moved to: X = " + mouseX + ", Y = " + mouseY);

        updateDistance();
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public void updateDistance(){
        double resultXCord = Math.abs(player.getPosX() - mouseX);
        double resultYCord = Math.abs(player.getPosY() - mouseY);
        double result = Math.sqrt((resultYCord)*(resultYCord) + (resultXCord)*(resultXCord));


        if (result > 1) {
            double speed = 200 / Math.sqrt(player.getMass());
            double ratio = speed / result;

            double newX = player.getPosX() + resultXCord * ratio;
            double newY = player.getPosY() + resultYCord * ratio;

            player.setPosX(newX);
            player.setPosY(newY);
        }
    }


}
