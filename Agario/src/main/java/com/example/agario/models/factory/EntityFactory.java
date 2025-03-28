package com.example.agario.models.factory;

import com.example.agario.models.Entity;

public abstract class EntityFactory {

    /**
     * abstract method for entity factory
     * @return Entity the entity created by the factory
     */
    public abstract Entity launchFactory();

}
