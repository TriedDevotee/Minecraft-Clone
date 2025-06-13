package org.example.entities;

import java.util.*;

public class EntityManager {
    private static final List<Entity> entities = new ArrayList<>();

    public static void addEntity(Entity e) {
        entities.add(e);
    }

    public static void updateAll(float deltaTime) {
        for (Entity e : entities) {
            e.update(deltaTime);
        }
    }

    public static void renderAll() {
        for (Entity e : entities) {
            e.render();
        }
    }

    public static List<Entity> getEntities() {
        return entities;
    }
}