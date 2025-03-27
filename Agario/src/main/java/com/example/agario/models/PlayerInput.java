package com.example.agario.models;

import java.io.Serializable;

public class PlayerInput implements Serializable {
    public double dirX;
    public double dirY;

    public PlayerInput(double dirX, double dirY) {
        this.dirX = dirX;
        this.dirY = dirY;
    }

    public PlayerInput() {
        this.dirX = 0;
        this.dirY = 0;
    }
}
