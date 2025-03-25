package com.example.agario.utils;

import com.example.agario.models.Player;
import com.example.agario.models.PlayerFactory;

import java.util.ArrayList;
import java.util.List;

public class Camera extends Dimension{

    Player player ;

    public Camera(Player player) {
        super(player.getPosX() - 100*Math.sqrt(player.getRadius())/2,
                player.getPosY() - 100*Math.sqrt(player.getRadius())/2,
                player.getPosX() + 100*Math.sqrt(player.getRadius())/2,
                player.getPosY() + 100*Math.sqrt(player.getRadius())/2);
        this.player = player ;
    }
    public List<Double> updateCameraPosition(double width , double height) {
        double centerX = player.getPosX();
        double centerY = player.getPosY();


        double offsetX = centerX - width / 2;
        double offsetY = centerY - height / 2;
        List<Double> list = new ArrayList<>();
        list.add(-offsetX);
        list.add(-offsetY);
        return list ;


    }


}
