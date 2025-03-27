package com.example.agario.models;

import com.example.agario.models.strategy.GluttonIA;
import com.example.agario.models.strategy.HunterIA;
import com.example.agario.models.strategy.RandomMovementIA;
import com.example.agario.models.strategy.Strategy;
import com.example.agario.models.utils.QuadTree;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class IA extends MovableEntity{

    private Strategy strategy;
    private QuadTree quadTree;
    private AtomicReference<Double> dx;
    private AtomicReference<Double> dy;

    public IA(double x, double y, QuadTree quadTree, Player player) {
        super(x, y,15);
        this.setName("FakePlayer"+ this.getId());

        this.quadTree = quadTree;

        List<Strategy> strategies = List.of(
                //new GluttonIA(this, quadTree)//,
                new HunterIA(this, quadTree, player)//,
                //new RandomMovementIA(this, quadTree.getDimension())
        );
        this.setStrategy(strategies.get(new Random().nextInt(strategies.size())));

        List<Double> newCoord = strategy.behaviorIA();
        dx = new AtomicReference<>(newCoord.get(0) - this.getPosX());
        dy = new AtomicReference<>(newCoord.get(1) - this.getPosY());
    }

    public void setStrategy(Strategy strategy){
        this.strategy = strategy;
    }

    public void setPositionIA(){
        List<Double> newCoord = strategy.behaviorIA();
        dx.set(newCoord.get(0) - this.getPosX());
        dy.set(newCoord.get(1) - this.getPosY());

        this.setSpeed(dx.get(), dy.get(), quadTree.getDimension().getxMax(), quadTree.getDimension().getyMax(), -1);
        this.updatePosition(dx.get(), dy.get(), quadTree.getDimension().getxMax(), quadTree.getDimension().getyMax());
    }
}
