package org.example.CameraControls;

import org.example.WorldRendering.Block;
import org.example.WorldRendering.Chunk;
import org.example.WorldRendering.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class Camera {
    public Vector3f position = new Vector3f(0, 1.3f, 5);
    public float yaw = -90f;
    public float pitch = 0f;

    public float speed = 5f;
    public float sensitivity = 0.2f;

    public Matrix4f getViewMatrix(){
        Vector3f front = new Vector3f(
                (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch)),
                (float) Math.sin(Math.toRadians(pitch)),
                (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch))
        ).normalize();

        Vector3f centre = new Vector3f(position).add(front);
        return new Matrix4f().lookAt(position, centre, new Vector3f(0,1,0));
    }

    public Raycasthit Raycast(Vector3f origin, Vector3f direction, float maxDistance, Chunk chunk){
        float step = 0.1f;

        Vector3f currentPos = new Vector3f(origin);

        Vector3i previousBlock = new Vector3i();

        for (float travelled = 0; travelled < maxDistance; travelled += step){
            currentPos.fma(step, direction);

            int bx = (int)Math.floor(currentPos.x);
            int by = (int)Math.floor(currentPos.y);
            int bz = (int)Math.floor(currentPos.z);

            if (by < 0 || by >= Chunk.CHUNK_HEIGHT){
                continue;
            }

            Block block = World.getBlockAt(bx, by, bz);
            if (block != null && block.isSolid){
                Vector3i blockPos = new Vector3i(bx, by, bz);

                Vector3i diff = new Vector3i(bx, by, bz).sub(previousBlock);
                Vector3f hitNormal = new Vector3f(diff.x, diff.y, diff.z);

                return new Raycasthit(blockPos, new Vector3f(currentPos), hitNormal);
            }
            previousBlock.set(bx, by, bz);
        }
        return null;
    }

    public Vector3f getViewDirection() {
        return new Vector3f(
                (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch)),
                (float) Math.sin(Math.toRadians(pitch)),
                (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch))
        ).normalize();
    }
}
