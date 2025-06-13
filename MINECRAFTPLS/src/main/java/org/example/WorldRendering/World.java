package org.example.WorldRendering;

import java.util.HashMap;
import java.util.Map;

public class World {

    // Key format: "x,z"
    public static final Map<String, Chunk> chunks = new HashMap<>();
    private static Block newBlock;

    // Add a chunk at a specific chunk coordinate
    public void addChunk(int chunkX, int chunkZ, Chunk chunk) {
        String key = getKey(chunkX, chunkZ);
        chunks.put(key, chunk);
    }

    // Get a chunk by chunk coordinates
    public static Chunk getChunk(int chunkX, int chunkZ) {
        return chunks.get(getKey(chunkX, chunkZ));
    }

    // Get a chunk using world coordinates
    public static Chunk getChunkAtWorld(int worldX, int worldZ) {
        int chunkX = Math.floorDiv(worldX, Chunk.CHUNK_SIZE);
        int chunkZ = Math.floorDiv(worldZ, Chunk.CHUNK_SIZE);
        return getChunk(chunkX, chunkZ);
    }

    // Get block at world coordinates
    public static Block getBlockAt(int worldX, int worldY, int worldZ) {
        if (worldY < 0 || worldY >= Chunk.CHUNK_HEIGHT) {
            return null;
        }

        Chunk chunk = getChunkAtWorld(worldX, worldZ);
        if (chunk == null) {
            return null;
        }

        int localX = Math.floorMod(worldX, Chunk.CHUNK_SIZE);
        int localZ = Math.floorMod(worldZ, Chunk.CHUNK_SIZE);

        return chunk.getBlock(localX, worldY, localZ);
    }

    private static String getKey(int chunkX, int chunkZ) {
        return chunkX + "," + chunkZ;
    }

    public static void removeBlockAt(int x, int y, int z) {
        Chunk chunk = getChunkAtWorld(x, z);
        if (chunk != null) {
            int localX = Math.floorMod(x, Chunk.CHUNK_SIZE);
            int localZ = Math.floorMod(z, Chunk.CHUNK_SIZE);
            chunk.removeBlock(localX, y, localZ);
            chunk.chunkMesh = new ChunkMesh(chunk);
        }
    }

    public static void addBlockAt(int x, int y, int z, String blockType, boolean immediateUpdate){
        Chunk chunk = getChunkAtWorld(x, z);
        if (chunk != null){
            int localX = Math.floorMod(x, chunk.CHUNK_SIZE);
            int localZ = Math.floorMod(z, chunk.CHUNK_SIZE);
            chunk.addBlock(localX, y, localZ, blockType);
            if(immediateUpdate) chunk.chunkMesh = new ChunkMesh(chunk);
        }
    }

    public static void placeTree(int x, int y, int z){
        for (int i = 0; i < 5; i++) {
            setBlock(x, y + i, z, "wood");
        }

        for (int dy = 2; dy <= 6; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= 2) {
                        setBlock(x + dx, y + dy, z + dz, "leaves");
                    }
                }
            }
        }

        updateChunksAround(x, z);
    }

    public static void setBlock(int x, int y, int z, String blockType) {
        Chunk chunk = getChunkAtWorld(x, z);
        if (chunk == null) return;

        int localX = Math.floorMod(x, Chunk.CHUNK_SIZE);
        int localZ = Math.floorMod(z, Chunk.CHUNK_SIZE);
        if (y >= 0 && y < Chunk.CHUNK_HEIGHT)
            newBlock = new Block(!blockType.equals("air"), localX, y, localZ, blockType);
            chunk.blocks[localX][y][localZ] = newBlock;
    }

    public static void updateChunksAround(int x, int z){
        int chunkX = x / Chunk.CHUNK_SIZE;
        int chunkZ = z / Chunk.CHUNK_SIZE;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Chunk chunk = getChunk(chunkX + dx, chunkZ + dz);
                if (chunk != null) {
                    chunk.chunkMesh = new ChunkMesh(chunk);
                }
            }
        }
    }
}