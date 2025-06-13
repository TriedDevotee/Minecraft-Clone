package org.example.entities;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class EntityRenderer {
    private final int vao, vbo;
    private int vertexCount;

    public EntityRenderer(String textureType) {
        float[] vertices = buildCubeVertices(textureType);
        vertexCount = vertices.length / 5;

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        int stride = 5 * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render(Vector3f position) {
        glPushMatrix();
        glTranslatef(position.x, position.y, position.z);

        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);

        glPopMatrix();
    }

    private float[] buildCubeVertices(String type) {
        float size = 1f;
        float half = size / 2f;

        float[] uv = getUVsForType(type);
        Vector3f[] faceOffsets = new Vector3f[]{
                new Vector3f(-half, -half, half),  new Vector3f(half, -half, half),
                new Vector3f(half, half, half),    new Vector3f(-half, half, half),
                new Vector3f(-half, -half, -half), new Vector3f(half, -half, -half),
                new Vector3f(half, half, -half),   new Vector3f(-half, half, -half)
        };

        int[] indices = {
                // Front
                0, 1, 2, 0, 2, 3,
                // Back
                5, 4, 7, 5, 7, 6,
                // Left
                4, 0, 3, 4, 3, 7,
                // Right
                1, 5, 6, 1, 6, 2,
                // Top
                3, 2, 6, 3, 6, 7,
                // Bottom
                4, 5, 1, 4, 1, 0
        };

        float[] vertices = new float[indices.length * 5];
        for (int i = 0; i < indices.length; i++) {
            Vector3f v = faceOffsets[indices[i]];
            int vi = i * 5;
            vertices[vi] = v.x;
            vertices[vi + 1] = v.y;
            vertices[vi + 2] = v.z;
            vertices[vi + 3] = uv[(i % 6) * 2];       // u
            vertices[vi + 4] = uv[(i % 6) * 2 + 1];   // v
        }

        return vertices;
    }

    private float[] getUVsForType(String type) {
        float sizeX = 1f / 80f;
        float sizeY = 1f / 45f;
        int col = 37, row = 0; // default to "cheese" block for example

        if (type.equals("sheep")) {
            col = 37; // example location for sheep texture
            row = 0;
        }

        float x = col * sizeX;
        float y = 1f - (row + 1) * sizeY;

        return new float[]{
                x, y + sizeY,
                x + sizeX, y + sizeY,
                x + sizeX, y,
                x, y + sizeY,
                x + sizeX, y,
                x, y
        };
    }
}
