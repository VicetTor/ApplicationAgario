package com.example.agario.models.specialPellet;

import com.example.agario.client.controllers.GameController;
import com.example.agario.models.Entity;
import com.example.agario.models.MovableEntity;

import java.util.Map;

public class InvisiblePellet extends SpecialPellet {
    public InvisiblePellet(double x, double y) {
        super(x, y);
        this.setMass(2);
    }
}
