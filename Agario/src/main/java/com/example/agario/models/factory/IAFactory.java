package com.example.agario.models.factory;

import com.example.agario.models.Entity;
import com.example.agario.models.IA;
import com.example.agario.utils.QuadTree;

import java.util.Random;

public class IAFactory extends EntityFactory{

    private double xMax;
    private double yMax;
    private QuadTree quadTree;

    public IAFactory(double xMax, double yMax, QuadTree quadTree){
        this.xMax = xMax;
        this.yMax = yMax;
        this.quadTree = quadTree;
    }
    @Override
    public Entity launchFactory() {
        return new IA(new Random().nextDouble(xMax), new Random().nextDouble(yMax), quadTree);
    }
}
