package org.example.InventoryHandler;

public class Inventory {
    public static final int inventorySize = 36;
    public InventorySlot[] slots = new InventorySlot[inventorySize];

    public Inventory(){
        for (int i = 0; i < inventorySize; i++){
            slots[i] = new InventorySlot("air", 0);
        }
    }

    public boolean AddItem(String blockType, int amount){
        for (InventorySlot slot : slots){
            if(slot.blockType.equals(blockType) && slot.count < 64){
                slot.count += amount;
                return true;
            }
        }

        for (InventorySlot slot : slots){
            if(slot.isEmpty()){
                slot.blockType = blockType;
                slot.count = amount;
                return true;
            }
        }

        return false;
    }
}
