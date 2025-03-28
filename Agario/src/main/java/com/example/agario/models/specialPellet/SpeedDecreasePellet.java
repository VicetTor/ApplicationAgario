package com.example.agario.models.specialPellet;

import com.example.agario.models.MovableEntity;

public class SpeedDecreasePellet extends SpecialPellet {

    /**
     * constructor for SpeedDecreasePellet
     * @param x horizontal position
     * @param y vertical position
     */
    public SpeedDecreasePellet(double x, double y) {
        super(x, y);
        this.setMass(2);
    }
}
