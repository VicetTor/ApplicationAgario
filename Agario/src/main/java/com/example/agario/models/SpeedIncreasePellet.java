package com.example.agario.models;

import com.example.agario.client.controllers.GameController;

public class SpeedIncreasePellet extends SpecialPellet{
    public SpeedIncreasePellet(double x, double y) {
        super(x, y);
        this.setMass(2);
    }
}
