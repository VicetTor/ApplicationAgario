package com.example.agario.models;

public class PlayerFactory extends EntityFactory{

    private String nom;

    public PlayerFactory(String nom){
        super();
        this.nom = nom;
    }

    @Override
    public Entity launchFactory() {
        return new Player(0,0, nom);
    }
}
