package com.example.agario.models.specialPellet;

import com.example.agario.models.MovableEntity;

public class SpeedIncreasePellet extends SpecialPellet {

    /**
     * constructor for SpeedIncreasePellet
     * @param x horizontal position
     * @param y vertical position
     */
    public SpeedIncreasePellet(double x, double y) {
        super(x, y);
        this.setMass(2);
    }
}
