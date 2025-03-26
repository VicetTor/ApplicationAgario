package com.example.agario.models;

import com.example.agario.models.strategy.GluttonIA;
import com.example.agario.models.strategy.HunterIA;
import com.example.agario.models.strategy.RandomMovementIA;
import com.example.agario.models.strategy.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IA extends MovableEntity{

    private Strategy strategy;

    public IA(double x, double y) {
        super(x, y,15);
        this.setName("Player "+ this.getId());

        List<Strategy> strategies = List.of(new GluttonIA(), new HunterIA(), new RandomMovementIA());
        this.setStrategy(strategies.get(new Random().nextInt(strategies.size())));
    }

    public void setStrategy(Strategy strategy){
        this.strategy = strategy;
    }

    public void IAstart(){
        strategy.behaviorIA();
    }
}
