package com.example.agario.client;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class PlayerInput implements EventHandler<MouseEvent> {

    double mouseX;
    double mouseY;

    public PlayerInput(){
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        mouseX = mouseEvent.getX();
        mouseY = mouseEvent.getY();

        //System.out.println("Mouse moved to: X = " + mouseX + ", Y = " + mouseY);

    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }



}
