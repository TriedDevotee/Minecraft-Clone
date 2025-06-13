package org.example.CameraControls;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class Raycasthit {
    public Vector3i blockPosition;
    public Vector3f hitPosition;
    public Vector3f hitNormal;

    public Raycasthit(Vector3i blockPos, Vector3f hitPos, Vector3f hitNorm){
        this.blockPosition = blockPos;
        this.hitPosition = hitPos;
        this.hitNormal = hitNorm;
    }
}
