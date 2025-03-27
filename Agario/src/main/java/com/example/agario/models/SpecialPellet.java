package com.example.agario.models;

import com.example.agario.client.controllers.GameController;

public abstract class SpecialPellet extends Pellet {
    public SpecialPellet(double x, double y) {
        super(x, y);
    }

    public abstract void doSpecialPelletAction(GameController g, MovableEntity movableEntity);

}
