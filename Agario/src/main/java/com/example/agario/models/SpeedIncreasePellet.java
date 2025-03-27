package com.example.agario.models;

import com.example.agario.controllers.GameController;

public class SpeedIncreasePellet extends SpecialPellet{
    public SpeedIncreasePellet(double x, double y) {
        super(x, y);
        this.setMass(2);
    }

    @Override
    public void doSpecialPelletAction(GameController g, MovableEntity movableEntity) {
        g.speedIncreaseEffect(movableEntity);
    }
}
