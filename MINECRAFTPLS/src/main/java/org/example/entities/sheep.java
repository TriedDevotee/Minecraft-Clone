package org.example.entities;

public class sheep extends Entity{
    private float velocityY = 0;
    private boolean onGround = false;

    public sheep(float x, float y, float z){
        this.posX = x;
        this.posY = y;
        this.posZ = z;


    }

    @Override
    public void update(float deltaTime) {
        velocityY -= 1 * deltaTime;
        posY += velocityY * deltaTime;

        /*if (posY < 35 + height / 2f) {
            posY = 35 + height / 2f;
            velocityY = 0;
            onGround = true;
        }*/

        for (int i = 0; i < 10; i++){
            float velocityX = 1;
            posX += velocityX;
        }

        for (int i = 0; i < 10; i++){
            float velocityZ = 1;
            posX += velocityZ;
        }

        for (int i = 0; i < 10; i++){
            float velocityX = -1;
            posX += velocityX;
        }

        for (int i = 0; i < 10; i++){
            float velocityZ = -1;
            posX += velocityZ;
        }
    }

    @Override
    public void render() {
        CubeMeshBuilder.renderTexturedCube(posX, posY, posZ, 3f, "cheese");
    }
}
