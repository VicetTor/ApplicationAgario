package com.example.agario.models.factory;

import com.example.agario.models.Entity;
import com.example.agario.models.Player;

import java.util.Random;

public class PlayerFactory extends EntityFactory{

    private String nom;
    private double height;
    private double width;

    public PlayerFactory(String nom, double width, double height){
        super();
        this.nom = nom;
        this.height = height;
        this.width = width;
    }

    @Override
    public Entity launchFactory() {
        return new Player(new Random().nextDouble(width),new Random().nextDouble(height), nom);
    }
}
