package com.example.agario.models;

import java.io.Serializable;

public class Player extends MovableEntity implements Serializable {

    public Player(double x, double y,String name) {
        super(x, y,15);
        this.setName(name);
    }

}
