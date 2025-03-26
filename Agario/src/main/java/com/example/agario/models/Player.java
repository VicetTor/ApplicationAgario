package com.example.agario.models;

public class Player extends MovableEntity{

    public Player(double x, double y,String name) {
        super(x, y,15);
        this.setName(name);
    }

}
