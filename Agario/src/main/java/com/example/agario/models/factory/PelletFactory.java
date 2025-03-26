package com.example.agario.models.factory;

import com.example.agario.models.Entity;
import com.example.agario.models.Pellet;

public class PelletFactory extends EntityFactory{

    private double x;

    private double y;

    public PelletFactory(double x, double y){
        super();
        this.x = x;
        this.y = y;
    }

    @Override
    public Entity launchFactory() {
        return new Pellet(x,y);
    }
}
