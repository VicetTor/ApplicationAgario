package com.example.agario.models;

import com.example.agario.models.strategy.GluttonIA;
import com.example.agario.models.strategy.HunterIA;
import com.example.agario.models.strategy.RandomMovementIA;
import com.example.agario.models.strategy.Strategy;
import com.example.agario.utils.QuadTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IA extends MovableEntity{

    private Strategy strategy;

    private QuadTree quadTree;

    public IA(double x, double y, QuadTree quadTree) {
        super(x, y,15);
        this.setName("FakePlayer "+ this.getId());

        this.quadTree = quadTree;
        List<Strategy> strategies = List.of(
                //new GluttonIA(this.getPosX(), this.getPosY(), quadTree),
                //new HunterIA(),
                new RandomMovementIA(this.getPosX(), this.getPosY(), quadTree.getDimension())
        );
        this.setStrategy(strategies.get(new Random().nextInt(strategies.size())));
    }

    public void setStrategy(Strategy strategy){
        this.strategy = strategy;
    }

    public void IAstart(){
        List<Double> newCoord = strategy.behaviorIA();
        this.setSpeed(newCoord.get(0), newCoord.get(1), quadTree.getDimension().getxMax(), quadTree.getDimension().getyMax());
        this.updatePosition(newCoord.get(0), newCoord.get(1));
    }
}
