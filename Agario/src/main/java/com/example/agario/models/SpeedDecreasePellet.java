package com.example.agario.models;

import com.example.agario.client.controllers.GameController;

public class SpeedDecreasePellet extends SpecialPellet{
    public SpeedDecreasePellet(double x, double y) {
        super(x, y);
        this.setMass(2);
    }
}
