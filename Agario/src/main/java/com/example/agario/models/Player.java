package com.example.agario.models;

import java.io.Serializable;

public class Player extends MovableEntity implements Serializable {
    private static final long serialVersionUID = -4421063672647629617L;

    public Player(double x, double y,String name) {
        super(x, y,15);
        this.setName(name);
    }

}
