package org.example.entities;

import org.example.WorldRendering.AABB;
import org.example.WorldRendering.Block;
import org.joml.Vector3f;

public abstract class Entity {
    public float posX, posY, posZ;
    public float width = 0.9f, height = 1.0f;

    public abstract void update(float deltaTime);
    public abstract void render();

    public boolean isCollidingWithBlock(Block block){
        AABB entityBox = new AABB(new Vector3f(posX, posY, posZ), width, height);
        AABB blockBox = new AABB(block.position, 1, 1);

        return entityBox.intersects(blockBox);
    }
}
