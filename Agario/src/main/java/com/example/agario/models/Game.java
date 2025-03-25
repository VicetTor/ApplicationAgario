package com.example.agario.models;

import com.example.agario.utils.QuadTree;

import java.util.HashMap;
import java.util.List;

public class Game {
    private QuadTree quadTree;

    public Game(QuadTree quadTree){
        this.quadTree = quadTree;
    }

    public QuadTree getQuadTree() {
        return quadTree;
    }

    public void UpdateWorld(){
        HashMap<Player, List<Entity>> playerEntities = new HashMap<Player, List<Entity>>();
    }
}
