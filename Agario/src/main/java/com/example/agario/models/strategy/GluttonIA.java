package com.example.agario.models.strategy;

import com.example.agario.models.Entity;
import com.example.agario.utils.Dimension;
import com.example.agario.utils.QuadTree;

import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.List;

public class GluttonIA implements Strategy{

    private Dimension dimension;

    private ArrayList<Entity> pelletsList;

    public GluttonIA(double x, double y, QuadTree quadTree){
        dimension = new Dimension(x-50,y-50,x+50,y+50);
        QuadTree.DFSChunk(quadTree, dimension, pelletsList);
        for(Entity pellet: pelletsList){
            pellet.getPosX();
            pellet.getPosY();
        }
    }
    @Override
    public List<Double> behaviorIA() {

        return new ArrayList<>();
    }
}

