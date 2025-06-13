package org.example.InventoryHandler;

public class InventorySlot {
    public String blockType;
    public int count;

    public InventorySlot(String blockType, int count){
        this.blockType = blockType;
        this.count = count;
    }

    public boolean isEmpty(){
        boolean empty = count <= 0;
        return empty;
    }
}
