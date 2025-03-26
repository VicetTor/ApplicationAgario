package com.example.agario.models;

import java.util.Random;

public class Pellet extends Entity{
    public Pellet(double x, double y) {
        super(x, y, new Random().nextInt(1, 11) / 10.0);
    }
}
