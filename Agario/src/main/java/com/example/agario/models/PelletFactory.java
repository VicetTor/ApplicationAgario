package com.example.agario.models;

public class PelletFactory extends EntityFactory{
    @Override
    public Entity launchFactory() {
        return new Pellet(0,0);
    }
}
