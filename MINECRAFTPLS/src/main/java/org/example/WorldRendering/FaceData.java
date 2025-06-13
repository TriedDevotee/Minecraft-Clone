package org.example.WorldRendering;

import org.joml.Vector3f;

public class FaceData {
    public static Vector3f[] getFaceVertices(Direction dir){
        switch (dir){
            case NORTH: return new Vector3f[] {
                    new Vector3f(0, 0, 0), new Vector3f(1, 1, 0), new Vector3f(1, 0, 0),
                    new Vector3f(0, 0, 0), new Vector3f(0, 1, 0), new Vector3f(1, 1, 0)
            };
            case EAST: return new Vector3f[] {
                    new Vector3f(1, 0, 1), new Vector3f(1, 1, 0), new Vector3f(1, 0, -1),
                    new Vector3f(1, 1, -1), new Vector3f(1, 1, 0), new Vector3f(1, 0, -1)
            };
            case SOUTH: return new Vector3f[] {
                    new Vector3f(1, 0, -1), new Vector3f(0, 1, -1), new Vector3f(0, 0, -1),
                    new Vector3f(0, 1, -1), new Vector3f(0, 1, -1), new Vector3f(0, 0, -1)
            };
            case WEST: return new Vector3f[] {
                    new Vector3f(1, 0, -1), new Vector3f(0, 1, -1), new Vector3f(0, 0, 0),
                    new Vector3f(0, 1, -1), new Vector3f(0, 1, -1), new Vector3f(0, 0, 0)
            };
            case UP: return new Vector3f[] {
                    new Vector3f(0, 1, 0), new Vector3f(1, 1, 0), new Vector3f(0, 1, -1),
                    new Vector3f(1, 1, -1), new Vector3f(1, 1, 0), new Vector3f(0, 1, -1)
            };
            case DOWN: return new Vector3f[] {
                    new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), new Vector3f(0, 0, -1),
                    new Vector3f(1, 0, -1), new Vector3f(1, 0, 0), new Vector3f(0, 0, -1)
            };
        }
        return null;
    }

    public static float[] getFaceUVs() {
        return new float[] {
                0, 0, 1, 1, 1, 0,
                0, 0, 0, 1, 1, 1
        };
    }
}
