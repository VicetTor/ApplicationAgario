package com.example.agario.utils;

import com.example.agario.models.Player;

public class Camera extends Dimension{

    public Camera(Player player) {
        super(player.getPosX(),
                player.getPosY() - 100*Math.sqrt(player.getRadius())/2,
                player.getPosX(), 
                player.getPosY() + 100*Math.sqrt(player.getRadius())/2);
    }


}
