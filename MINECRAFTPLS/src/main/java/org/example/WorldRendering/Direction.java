package org.example.WorldRendering;
import org.joml.Vector3f;

public enum Direction {
    UP(0, 1, 0), DOWN(0, -1, 0),
    NORTH(0, 0, -1), SOUTH(0, 0, 1),
    EAST(1, 0, 0), WEST(-1, 0, 0);

    public final int dx, dy, dz;

    Direction(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public Vector3f[] getFaceVertices(float x, float y, float z) {
        switch (this) {
            case UP: return new Vector3f[]{
                    new Vector3f(x,     y+1, z+1), // BL
                    new Vector3f(x+1,   y+1, z+1), // BR
                    new Vector3f(x+1,   y+1, z),   // TR
                    new Vector3f(x,     y+1, z+1), // BL
                    new Vector3f(x+1,   y+1, z),   // TR
                    new Vector3f(x,     y+1, z)    // TL
            };
            case DOWN: return new Vector3f[]{
                    new Vector3f(x,     y, z),     // BL
                    new Vector3f(x+1,   y, z),     // BR
                    new Vector3f(x+1,   y, z+1),   // TR
                    new Vector3f(x,     y, z),     // BL
                    new Vector3f(x+1,   y, z+1),   // TR
                    new Vector3f(x,     y, z+1)    // TL
            };
            case NORTH: return new Vector3f[]{
                    new Vector3f(x+1,   y,   z),   // BL
                    new Vector3f(x+1,   y+1, z),   // BR
                    new Vector3f(x,     y+1, z),   // TR
                    new Vector3f(x+1,   y,   z),   // BL
                    new Vector3f(x,     y+1, z),   // TR
                    new Vector3f(x,     y,   z)    // TL
            };
            case SOUTH: return new Vector3f[]{
                    new Vector3f(x,     y,   z+1), // BL
                    new Vector3f(x,     y+1, z+1), // BR
                    new Vector3f(x+1,   y+1, z+1), // TR
                    new Vector3f(x,     y,   z+1), // BL
                    new Vector3f(x+1,   y+1, z+1), // TR
                    new Vector3f(x+1,   y,   z+1)  // TL
            };
            case EAST: return new Vector3f[]{
                    new Vector3f(x+1, y,   z+1),   // BL
                    new Vector3f(x+1, y+1, z+1),   // BR
                    new Vector3f(x+1, y+1, z),     // TR
                    new Vector3f(x+1, y,   z+1),   // BL
                    new Vector3f(x+1, y+1, z),     // TR
                    new Vector3f(x+1, y,   z)      // TL
            };
            case WEST: return new Vector3f[]{
                    new Vector3f(x, y,   z),       // BL
                    new Vector3f(x, y+1, z),       // BR
                    new Vector3f(x, y+1, z+1),     // TR
                    new Vector3f(x, y,   z),       // BL
                    new Vector3f(x, y+1, z+1),     // TR
                    new Vector3f(x, y,   z+1)      // TL
            };
        }
        return null;
    }
}
