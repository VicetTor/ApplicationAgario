package com.example.agario.models;

public class IAFactory extends EntityFactory{
    @Override
    public Entity launchFactory() {
        return new IA(0,0);
    }
}
