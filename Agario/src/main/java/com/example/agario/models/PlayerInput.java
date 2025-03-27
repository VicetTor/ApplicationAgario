package com.example.agario.models;

import java.io.Serializable;

public class PlayerInput implements Serializable {
    private static final long serialVersionUID = 1;
    public double dirX;
    public double dirY;

    public double speed;

    public PlayerInput(double dirX, double dirY,double speed) {
        this.dirX = dirX;
        this.dirY = dirY;
        this.speed = speed ;
    }

    public PlayerInput() {
        this.dirX = 0;
        this.dirY = 0;
    }
}
