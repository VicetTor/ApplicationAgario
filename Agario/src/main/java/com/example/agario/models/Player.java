package com.example.agario.models;

public class Player extends MovableEntity{
    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id ;
    public Player(double x, double y,String name) {
        super(x, y,15);
        this.setName(name);
    }



}
