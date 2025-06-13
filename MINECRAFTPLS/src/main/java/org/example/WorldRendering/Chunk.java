package org.example.WorldRendering;


import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Objects.hash;

public class Chunk {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_HEIGHT = 50;

    public Block[][][] blocks = new Block[CHUNK_SIZE][CHUNK_HEIGHT][CHUNK_SIZE];
    public int chunkX, chunkZ;
    public ChunkMesh chunkMesh;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

        assembleChunk();
        //decorateTrees();
        chunkMesh = new ChunkMesh(this);
    }

    public void assembleChunk() {
        int offsetX = chunkX * CHUNK_SIZE;

        for (int y = 0; y < CHUNK_HEIGHT; y++) {
            String type;
            if (y < 2) type = "bedrock";
            else if (y < 30) type = "stone";
            else if (y < 34) type = "dirt";
            else if (y < 35) type = "grass";
            else type = "air";

            for (int x = 0; x < CHUNK_SIZE; x++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    if (!type.equals("air")) {
                        blocks[x][y][z] = new Block(true, x, y, z, type);
                    } else {
                        blocks[x][y][z] = new Block(false, x, y, z, type);
                    }

                }
            }

        }


            addOres("coal", 8);
            addOres("iron", 7);
            addOres("lapis", 6);
            addOres("copper", 4);
            addOres("redstone", 3);
            addOres("gold", 3);
            addOres("emerald", 1);
            addOres("diamond", 1);
    }

    private void addOres(String blockType, int upperBound){
        int oreCount = ThreadLocalRandom.current().nextInt(0, upperBound);
        System.out.println("Spawned "+oreCount+" instances of "+blockType+" ore");
        for (int i = 0; i < oreCount; i++){
            int xPlace = ThreadLocalRandom.current().nextInt(0, 16);
            int zPlace = ThreadLocalRandom.current().nextInt(0, 16);
            int yPlace = ThreadLocalRandom.current().nextInt(0, 28);

            System.out.println("Generated ore at: x - "+xPlace+" y - "+yPlace+" z - "+zPlace);
            blocks[xPlace][yPlace][zPlace] = new Block(true, xPlace, yPlace, zPlace, blockType);
        }
    }

    public Block getBlock(int x, int y, int z){
        Block b = blocks[x][y][z];
        return b;
    }

    public void removeBlock(int x, int y, int z){
        blocks[x][y][z] = new Block(false, x, y, z, "air");
        chunkMesh = new ChunkMesh(this);
    }

    public void addBlock(int x, int y, int z, String type){
        Block block = new Block(true, x, y, z, type);
        blocks[x][y][z] = block;
        chunkMesh = new ChunkMesh(this);
    }

    public void decorateTrees(){
        Random rand = new Random(hash(chunkX, chunkZ));
        int treeCount = rand.nextInt(0, 3);

        for (int i = 0; i < treeCount; i++) {
            int localX = rand.nextInt(CHUNK_SIZE);
            int localZ = rand.nextInt(CHUNK_SIZE);

            int worldX = chunkX * CHUNK_SIZE + localX;
            int worldZ = chunkZ * CHUNK_SIZE + localZ;
            int worldY = 34;

            if (!blocks[localX][worldY][localZ].blockType.equals("grass")) continue;

            System.out.println("Decorating chunk at " + chunkX + ", " + chunkZ);
            System.out.println("Tree count: " + treeCount);


            World.placeTree(worldX, worldY + 1, worldZ);
        }
    }

    public static int hash(int x, int z) {
        long seed = 0xdeadbeefL; // your world seed
        long result = x * 341873128712L + z * 132897987541L + seed;
        return (int)(result ^ (result >>> 32));
    }
}
