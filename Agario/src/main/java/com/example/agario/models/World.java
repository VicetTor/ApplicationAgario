package com.example.agario.models;

import com.example.agario.utils.QuadTree;

import java.util.HashMap;
import java.util.List;

public class World {
    private QuadTree quadTree;

    public World(QuadTree quadTree){
        this.quadTree = quadTree;
    }

    public QuadTree getQuadTree() {
        return quadTree;
    }

    public void UpdateWorld(){
        HashMap<Player, List<Entity>> playerEntities = new HashMap<Player, List<Entity>>();
    }
}
