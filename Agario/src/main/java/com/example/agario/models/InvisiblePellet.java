package com.example.agario.models;

import com.example.agario.client.controllers.GameController;

public class InvisiblePellet extends SpecialPellet{
    public InvisiblePellet(double x, double y) {
        super(x, y);
        this.setMass(2);
    }
}
