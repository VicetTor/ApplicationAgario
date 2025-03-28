package com.example.agario.models;

import java.util.Random;

public class Pellet extends Entity{

    /**
     * constructor for Pellet
     * @param x Pellet X coo
     * @param y Pellet Y coo
     */
    public Pellet(double x, double y) {
        super(x, y, new Random().nextInt(1, 11) / 10.0);
    }
}
