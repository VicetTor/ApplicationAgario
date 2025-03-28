package com.example.agario.models.specialPellet;

import com.example.agario.models.MovableEntity;
import com.example.agario.models.Pellet;

public abstract class SpecialPellet extends Pellet {

    /**
     * constructor for SpecialPellet
     * @param x horizontal position
     * @param y vertical position
     */
    public SpecialPellet(double x, double y) {
        super(x, y);
    }
}
