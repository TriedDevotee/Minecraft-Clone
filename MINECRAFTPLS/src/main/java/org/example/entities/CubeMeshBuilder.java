package org.example.entities;

import org.example.WorldRendering.Direction;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.*;

public class CubeMeshBuilder {
    public static void renderTexturedCube(float x, float y, float z, float size, String textureName) {
        Vector3f pos = new Vector3f(x, y, z);
        float half = size / 2f;
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 37);

        for (Direction dir : Direction.values()) {
            Vector3f[] vertices = dir.getFaceVertices((x - half), (y - half), (z - half));
            float[] uv = getUVsForType(textureName);

            glBegin(GL_TRIANGLES);
            for (int i = 0; i < 6; i++) {
                glTexCoord2f(uv[i * 2], uv[i * 2 + 1]);
                glVertex3f(vertices[i].x, vertices[i].y, vertices[i].z);
            }
            glEnd();
        }
    }

    private static float[] getUVsForType(String type) {
        float sizeX = 1f / 80f;
        float sizeY = 1f / 45f;
        int col = 0, row = 0;

        switch (type) {
            case "cheese": col = 37; row = 0; break;
            // add more as needed
        }

        float x = col * sizeX;
        float y = 1f - ((row + 1) * sizeY);

        return new float[] {
                x, y + sizeY,     x + sizeX, y + sizeY,   x + sizeX, y,
                x, y + sizeY,     x + sizeX, y,           x, y
        };
    }
}
