package org.example.WorldRendering;

import org.joml.Vector3f;

public class AABB {
    public Vector3f min;
    public Vector3f max;

    public AABB(Vector3f position, float width, float height){
        this.min = new Vector3f(position.x - width / 2, position.y, position.z - width / 2);
        this.max = new Vector3f(position.x + width / 2, position.y + height, position.z + width / 2);
    }

    public boolean intersects(AABB other){
        boolean intersection = true;

        if(other.max.x < min.x || other.min.x > max.x
                || other.max.y < min.y || other.min.y > max.y
                || other.max.z < min.z || other.min.z > max.z)
            intersection = false;

        return intersection;
    }

    public boolean intersectsFeet(AABB other) {
        float epsilon = 0.05f; // small threshold for "standing on"

        boolean xOverlap = this.max.x > other.min.x && this.min.x < other.max.x;
        boolean zOverlap = this.max.z > other.min.z && this.min.z < other.max.z;

        boolean touchingTop = Math.abs(this.min.y - other.max.y) < epsilon;

        if (xOverlap && zOverlap && touchingTop) {
            System.out.println("Feet intersection confirmed with block at Y=" + other.max.y);
        }

        return xOverlap && zOverlap && touchingTop;
    }
}
