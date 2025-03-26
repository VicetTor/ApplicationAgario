package com.example.agario.models.strategy;

import com.example.agario.models.Entity;
import com.example.agario.utils.Dimension;
import com.example.agario.utils.QuadTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomMovementIA implements Strategy{

    private double x;
    private double y;
    private Dimension dimension;

    public RandomMovementIA(double x, double y, Dimension dimension){
        this.x = x;
        this.y = y;
        this.dimension = dimension;
    }

    @Override
    public List<Double> behaviorIA() {
        Random rand = new Random();
        return List.of(rand.nextDouble(dimension.getxMax()), rand.nextDouble(dimension.getyMax()));
    }
}

