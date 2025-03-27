package com.example.agario.models;

import com.example.agario.controllers.GameController;

public class InvisiblePellet extends SpecialPellet{
    public InvisiblePellet(double x, double y) {
        super(x, y);
        this.setMass(2);
    }

    @Override
    public void doSpecialPelletAction(GameController g, MovableEntity movableEntity) {
        g.invisiblePelletEffect(movableEntity);
    }
}
