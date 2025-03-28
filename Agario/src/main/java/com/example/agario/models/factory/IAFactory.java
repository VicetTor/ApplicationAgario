package com.example.agario.models.factory;

import com.example.agario.models.Entity;
import com.example.agario.models.IA;
import com.example.agario.models.Player;
import com.example.agario.models.utils.QuadTree;

import java.util.List;
import java.util.Random;

public class IAFactory extends EntityFactory{

    private double xMax;
    private double yMax;
    private QuadTree quadTree;
    private Player player;
    private List<Entity> entities;

    /**
     * constructor for IAFactory
     * @param xMax quadtree's max dimensions horizontally
     * @param yMax quadtree's max dimensions vertically
     * @param quadTree quadtree
     * @param player player
     * @param entities entities list
     */
    public IAFactory(double xMax, double yMax, QuadTree quadTree, Player player, List<Entity> entities){
        this.xMax = xMax;
        this.yMax = yMax;
        this.quadTree = quadTree;
        this.player = player;
        this.entities = entities;
    }

    /**
     * abstract method for AI factory
     * @return Entity the AI created by the factory
     */
    @Override
    public Entity launchFactory() {
        return new IA(new Random().nextDouble(xMax), new Random().nextDouble(yMax), quadTree, player, entities);
    }
}
