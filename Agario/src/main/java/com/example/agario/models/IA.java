package com.example.agario.models;

public class IA extends MovableEntity{

    public IA(double x, double y) {
        super(x, y,50);
        this.setName("Player "+ this.getId());
    }
}
