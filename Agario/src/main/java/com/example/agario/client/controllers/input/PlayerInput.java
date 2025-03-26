package com.example.agario.client.controllers.input;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class PlayerInput implements EventHandler<MouseEvent> {

    double mouseX;
    double mouseY;
    private double targetX;
    private double targetY;

    public PlayerInput(){
    }

    public PlayerInput(double targetX, double targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }

    // Getters et setters
    public double getTargetX() {
        return targetX;
    }

    public void setTargetX(double targetX) {
        this.targetX = targetX;
    }

    public double getTargetY() {
        return targetY;
    }

    public void setTargetY(double targetY) {
        this.targetY = targetY;
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
