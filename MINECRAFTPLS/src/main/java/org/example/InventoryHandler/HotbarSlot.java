package org.example.InventoryHandler;

public class HotbarSlot {
    public String blockType;
    public int count;

    public HotbarSlot(String blockType, int count){
        this.blockType = blockType;
        this.count = count;
    }

    public boolean isEmpty(){
        boolean empty = count <= 0;
        return empty;
    }
}
