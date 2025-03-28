package com.example.agario.models.specialPellet;

import com.example.agario.models.MovableEntity;

public class SpeedDecreasePellet extends SpecialPellet {
    public SpeedDecreasePellet(double x, double y) {
        super(x, y);
        this.setMass(2);
    }
}
