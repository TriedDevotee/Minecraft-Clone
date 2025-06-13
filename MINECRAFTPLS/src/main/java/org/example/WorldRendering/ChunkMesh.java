package org.example.WorldRendering;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;


public class ChunkMesh {
    public FloatBuffer meshBuffer;
    public int vertexCount, vao, vbo;
    public final int vectorSize = 5; // x y z u d

    public ChunkMesh(Chunk chunk){

        meshBuffer = BufferUtils.createFloatBuffer(Chunk.CHUNK_SIZE * Chunk.CHUNK_HEIGHT * Chunk.CHUNK_SIZE * 6 * 6 * vectorSize);

        for (int x = 0; x < Chunk.CHUNK_SIZE; x++){
            for (int y = 0; y < Chunk.CHUNK_HEIGHT; y++){
                for (int z = 0; z < Chunk.CHUNK_SIZE; z++){
                    for (Direction dir : Direction.values()){
                        Block block = chunk.blocks[x][y][z];
                        if (block == null || !block.isSolid) continue;
                        if (isFaceVisible(chunk.blocks, x,y,z, dir)){
                            String textureName = switch (dir){
                                case UP -> block.topTexture;
                                case DOWN -> block.bottomTexture;
                                default -> block.sideTexture;
                            };
                            AddFace(x, y, z, dir, textureName);
                        }
                    }
                }
            }
        }
        meshBuffer.flip();
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, meshBuffer, GL_STATIC_DRAW);

        //VERTEX MAKEUP - POSITION 3, TEXTURE 2
        int stride = 5 * Float.BYTES;

        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1,2, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        vertexCount = meshBuffer.limit() / vectorSize;


    }

    private void AddFace(int x, int y, int z, Direction dir, String textureName){
        Vector3f[] vertices = dir.getFaceVertices(x, y, z);
        float[] uv = getUVsForType(textureName);
        for (int i = 0; i < 3; i++) {
            uv = rotateUVs90(uv);
        }

        for (int i = 0; i < 6; i++){
            Vector3f v = vertices[i];
            meshBuffer.put(v.x).put(v.y).put(v.z);
            meshBuffer.put(uv[2 * i]).put(uv[2 * i + 1]);
        }
    }

    private float[] rotateUVs90(float[] uv) {
        // First, find min and max U and V from original UVs:
        float minU = Float.MAX_VALUE, maxU = -Float.MAX_VALUE;
        float minV = Float.MAX_VALUE, maxV = -Float.MAX_VALUE;
        for (int i = 0; i < 6; i++) {
            float u = uv[2*i];
            float v = uv[2*i + 1];
            if (u < minU) minU = u;
            if (u > maxU) maxU = u;
            if (v < minV) minV = v;
            if (v > maxV) maxV = v;
        }

        float tileWidth = maxU - minU;
        float tileHeight = maxV - minV;

        float[] rotated = new float[uv.length];
        for (int i = 0; i < 6; i++) {
            float u = uv[2*i];
            float v = uv[2*i + 1];

            // Convert to local tile coords [0..1]
            float localU = (u - minU) / tileWidth;
            float localV = (v - minV) / tileHeight;

            // Rotate 90° clockwise around tile center:
            // newU = localV
            // newV = 1 - localU

            float newU = localV;
            float newV = 1f - localU;

            // Map back to atlas coords
            rotated[2*i] = minU + newU * tileWidth;
            rotated[2*i + 1] = minV + newV * tileHeight;
        }
        return rotated;
    }

    boolean isFaceVisible(Block[][][] blocks, int x, int y, int z, Direction dir){
        int newX = x + dir.dx;
        int newY = y + dir.dy;
        int newZ = z + dir.dz;

        if (newX < 0 || newX >= Chunk.CHUNK_SIZE
                || newY < 0 || newY >= Chunk.CHUNK_HEIGHT
                || newZ < 0 || newZ >= Chunk.CHUNK_SIZE){
            return true; //CHECKS FOR CHUNK BOUNDARIES
        }
        Block neighbour = blocks[newX][newY][newZ];
        return neighbour == null || !neighbour.isSolid;
    }

    private float[] getUVsForType(String type) {
        float sizeX = 1f / 80f;
        float sizeY = 1f / 45f;
        int col = 0;
        int row = 0;

        switch (type) {
            case "grass": col = 3; row = 0; break;
            case "dirt": col = 1; row = 0; break;
            case "stone": col = 2; row = 0; break;
            case "bedrock": col = 0; row = 0; break;
            case "wood": col = 4; row = 0; break;
            case "leaves": col = 5; row = 0; break;
            case "obsidian": col = 6; row = 0; break;
            case "diamond": col = 7; row = 0; break;
            case "gold": col = 8; row = 0; break;
            case "coal": col = 9; row = 0; break;
            case "lapis": col = 10; row = 0; break;
            case "copper": col = 11; row = 0; break;
            case "iron": col = 12; row = 0; break;
            case "redstone": col = 13; row = 0; break;
            case "emerald": col = 14; row = 0; break;
            case "yellowconcrete": col = 15; row = 0; break;
            case "whiteconcrete": col = 16; row = 0; break;
            case "silverconcrete": col = 17; row = 0; break;
            case "redconcrete": col = 18; row = 0; break;
            case "purpleconcrete": col = 19; row = 0; break;
            case "pinkconcrete": col = 20; row = 0; break;
            case "orangeconcrete": col = 21; row = 0; break;
            case "magentaconcrete": col = 22; row = 0; break;
            case "limeconcrete": col = 23; row = 0; break;
            case "lightblueconcrete": col = 24; row = 0; break;
            case "greenconcrete": col = 25; row = 0; break;
            case "greyconcrete": col = 26; row = 0; break;
            case "cyanconcrete": col = 27; row = 0; break;
            case "brownconcrete": col = 28; row = 0; break;
            case "blueconcrete": col = 29; row = 0; break;
            case "blackconcrete": col = 30; row = 0; break;
            case "netherbrick": col = 31; row = 0; break;
            case "brick": col = 32; row = 0; break;
            case "prismarine": col = 33; row = 0; break;
            case "darkprismarine": col = 34; row = 0; break;
            case "prismarinebrick": col = 35; row = 0; break;
            case "stonebrick": col = 36; row = 0; break;
            case "cheese": col = 37; row = 0; break;
            case "sadobsidian": col = 38; row = 0; break;
            case "cobblestone": col = 39; row = 0; break;
            case "amethyst": col = 40; row = 0; break;
            case "portal": col = 41; row = 41; break;
            case "diamondblock": col = 42; row = 0; break;
            case "calcite": col = 43; row = 0; break;
            case "white": col = 44; row = 0; break;
            case "woodtop": col = 45; row = 0; break;
            case "birch": col = 46; row = 0; break;
            case "spruce": col = 47; row = 0; break;
            case "jungle": col = 48; row = 0; break;
            case "darkoak": col = 49; row = 0; break;
            case "acacia": col = 50; row = 0; break;
            case "cherry": col = 51; row = 0; break;
            case "mangrove": col = 52; row = 0; break;
            case "planks": col = 53; row = 0; break;
            case "birchplanks": col = 54; row = 0; break;
            case "spruceplanks": col = 55; row = 0; break;
            case "jungleplanks": col = 56; row = 0; break;
            case "darkoakplanks": col = 57; row = 0; break;
            case "acaciaplanks": col = 58; row = 0; break;
            case "cherryplanks": col = 59; row = 0; break;
            case "mangroveplanks": col = 60; row = 0; break;
            case "tuff": col = 61; row = 0; break;
            case "tuffbricks": col = 62; row = 0; break;
            case "deepslate": col = 63; row = 0; break;
            case "deepslatebricks": col = 64; row = 0; break;
            case "copperblock": col = 65; row = 0; break;
            case "ironblock": col = 66; row = 0; break;
            case "goldblock": col = 67; row = 0; break;
            case "emeraldblock": col = 68; row = 0; break;
            case "lapisblock": col = 69; row = 0; break;
            case "redstoneblock": col = 70; row = 0; break;
            case "coalblock": col = 71; row = 0; break;
            case "basalt": col = 72; row = 0; break;
            case "bone": col = 73; row = 0; break;
            case "sand": col = 74; row = 0; break;
            case "gravel": col = 75; row = 0; break;
            case "ice": col = 76; row = 0; break;
            case "packedice": col = 77; row = 0; break;
            case "blueice": col = 78; row = 0; break;
            case "craftingtable": col = 79; row = 0; break;
            case "mud": col = 0; row = 1; break;
            case "quartz": col = 1; row = 1; break;
            case "quartzbrick": col = 2; row = 1; break;
            case "cheesebrick": col = 3; row = 1; break;
            case "yellowcoral": col = 4; row = 1; break;
            case "redcoral": col = 5; row = 1; break;
            case "purplecoral": col = 6; row = 1; break;
            case "pinkcoral": col = 7; row = 1; break;
            case "bluecoral": col = 8; row = 1; break;
            case "mossycobblestone": col = 9; row = 1; break;
            case "netherrack": col = 10; row = 1; break;
            case "birchtop": col = 11; row = 1; break;
            case "sprucetop": col = 12; row = 1; break;
            case "jungletop": col = 13; row = 1; break;
            case "darkoaktop": col = 14; row = 1; break;
            case "acaciatop": col = 15; row = 1; break;
            case "cherrytop": col = 16; row = 1; break;
            case "mangrovetop": col = 17; row = 1; break;
            case "bonetop": col = 18; row = 1; break;
            case "basalttop": col = 19; row = 1; break;
            case "craftingtabletop": col = 20; row = 1; break;
        }

        float x = col * sizeX;
        float y = 1f - ((row + 1) * sizeY); // Only 1 row

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
