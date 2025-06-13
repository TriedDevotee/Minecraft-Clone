package org.example.WorldRendering;

import org.joml.Vector3f;

public class Block {
    public boolean isSolid;
    public Vector3f position;
    public int texture;
    public String blockType;

    public String topTexture;
    public String sideTexture;
    public String bottomTexture;

    public Block(boolean solid, int x, int y, int z, String type){
        this.position = new Vector3f(x, y, z);
        this.isSolid = solid;
        this.blockType = type;
        this.texture = 0;

        if (blockType.equals("wood")) {
            topTexture = "woodtop";
            bottomTexture = "woodtop";
            sideTexture = "wood";
        } else if (blockType.equals("birch")) {
            topTexture = "birchtop";
            bottomTexture = "birchtop";
            sideTexture = "birch";
        }else if (blockType.equals("spruce")) {
            topTexture = "sprucetop";
            bottomTexture = "sprucetop";
            sideTexture = "spruce";
        }else if (blockType.equals("jungle")) {
            topTexture = "jungletop";
            bottomTexture = "jungletop";
            sideTexture = "jungle";
        }else if (blockType.equals("acacia")) {
            topTexture = "acaciatop";
            bottomTexture = "acaciatop";
            sideTexture = "acacia";
        }else if (blockType.equals("darkoak")) {
            topTexture = "darkoaktop";
            bottomTexture = "darkoaktop";
            sideTexture = "darkoak";
        }else if (blockType.equals("cherry")) {
            topTexture = "cherrytop";
            bottomTexture = "cherrytop";
            sideTexture = "cherry";
        }else if (blockType.equals("mangrove")) {
            topTexture = "mangrovetop";
            bottomTexture = "mangrovetop";
            sideTexture = "mangrove";
        }else if (blockType.equals("craftingtable")) {
            topTexture = "craftingtabletop";
            bottomTexture = "planks";
            sideTexture = "craftingtable";
        }else if (blockType.equals("bone")) {
            topTexture = "bonetop";
            bottomTexture = "bonetop";
            sideTexture = "bone";
        }else if (blockType.equals("basalt")) {
            topTexture = "basalttop";
            bottomTexture = "basalttop";
            sideTexture = "basalt";
        }else {
            topTexture = blockType;
            bottomTexture = blockType;
            sideTexture = blockType;
        }
    }
}
