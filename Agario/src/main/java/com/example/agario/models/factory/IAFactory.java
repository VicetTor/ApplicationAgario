package com.example.agario.models.factory;

import com.example.agario.models.Entity;
import com.example.agario.models.IA;
import com.example.agario.models.Player;
import com.example.agario.models.utils.QuadTree;

import java.util.Random;

public class IAFactory extends EntityFactory{

    private double xMax;
    private double yMax;
    private QuadTree quadTree;
    private Player player;

    public IAFactory(double xMax, double yMax, QuadTree quadTree, Player player){
        this.xMax = xMax;
        this.yMax = yMax;
        this.quadTree = quadTree;
        this.player = player;
    }
    @Override
    public Entity launchFactory() {
        return new IA(new Random().nextDouble(xMax), new Random().nextDouble(yMax), quadTree, player);
    }
}
