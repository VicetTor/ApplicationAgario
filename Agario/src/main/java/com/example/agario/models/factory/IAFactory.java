package com.example.agario.models.factory;

import com.example.agario.models.Entity;
import com.example.agario.models.IA;

public class IAFactory extends EntityFactory{
    @Override
    public Entity launchFactory() {
        return new IA(0,0);
    }
}
