package com.example.agario.models;

import com.example.agario.input.PlayerInput;
import com.example.agario.utils.Dimension;

public class Player extends MovableEntity{

    public Player(double x, double y,String name) {
        super(x, y,50);
        this.setName(name);
    }

}
