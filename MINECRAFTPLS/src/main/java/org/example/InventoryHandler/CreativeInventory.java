package org.example.InventoryHandler;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CreativeInventory {
    public List<String> blockStorage;
    public final int NUM_BLOCKS = 90;

    public CreativeInventory(){
        blockStorage = new ArrayList<>();
        assembleBlocks();
    }

    public void assembleBlocks(){
        String iconPath = "src/main/resources/AppIcons";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(iconPath))) {
            for (Path entry : stream){
                if (Files.isRegularFile(entry)){
                    String pathName = entry.getFileName().toString();
                    pathName = pathName.replaceFirst(".png", "");
                    System.out.println(pathName);
                    blockStorage.add(pathName);
                }
            }

        } catch (IOException e){
            return;
        }

        for (int i = blockStorage.size(); i < NUM_BLOCKS; i++){
            blockStorage.add("stone");
        }

    }
}
