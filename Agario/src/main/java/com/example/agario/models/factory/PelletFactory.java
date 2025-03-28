package com.example.agario.models.factory;

import com.example.agario.models.*;
import com.example.agario.models.specialPellet.InvisiblePellet;
import com.example.agario.models.specialPellet.SpeedDecreasePellet;
import com.example.agario.models.specialPellet.SpeedIncreasePellet;

import java.util.Random;

public class PelletFactory extends EntityFactory{

    private double x;

    private double y;

    /**
     * constructor for PelletFactory
     * @param x position horizontally
     * @param y position vertically
     */
    public PelletFactory(double x, double y){
        super();
        this.x = x;
        this.y = y;
    }

    /**
     * abstract method for Pellet factory, with a random type of pellet
     * @return Entity the Pellet created by the factory
     */
    @Override
    public Entity launchFactory() {
        int randomNumber = new Random().nextInt(1000+1);

        if(randomNumber < 1){
            return new InvisiblePellet(x,y);
        }
        else if(randomNumber < 2){
            return new SpeedIncreasePellet(x,y);
        }
        else if(randomNumber < 3){
            return new SpeedDecreasePellet(x,y);
        }
        else {
            return new Pellet(x, y);
        }
    }
}
