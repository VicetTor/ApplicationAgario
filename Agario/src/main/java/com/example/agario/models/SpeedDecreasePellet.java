package com.example.agario.models;

import com.example.agario.controllers.GameController;

public class SpeedDecreasePellet extends SpecialPellet{
    public SpeedDecreasePellet(double x, double y) {
        super(x, y);
        this.setMass(2);
    }

    @Override
    public void doSpecialPelletAction(GameController g, MovableEntity movableEntity) {
        g.speedDecreaseEffect(movableEntity);
    }
}
