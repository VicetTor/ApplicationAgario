package com.example.agario.models.factory;

import com.example.agario.models.Entity;
import com.example.agario.models.Player;

import java.util.Random;

public class PlayerFactory extends EntityFactory{

    private String nom;
    private int height;
    private int width;

    public PlayerFactory(String nom, int width, int height){
        super();
        this.nom = nom;
        this.height = height;
        this.width = width;
    }

    @Override
    public Entity launchFactory() {
        return new Player(new Random().nextInt(width),new Random().nextInt(height), nom);
    }
}
