package org.example;
import org.example.CameraControls.Camera;
import org.example.CameraControls.Raycasthit;
import org.example.InventoryHandler.CreativeInventory;
import org.example.InventoryHandler.HotbarSlot;
import org.example.WorldRendering.*;
import org.example.entities.CubeMeshBuilder;
import org.example.entities.EntityManager;
import org.example.entities.sheep;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;


public class Main {
    private long window;
    private Camera camera;
    private float lastX = 400, lastY = 300;
    private boolean firstMouse = true;
    private int atlasTexture;
    private final float playerWidth = 0.3f, playerHeight = 1.3f;
    private Vector3f velocity = new Vector3f();
    private boolean isGrounded = false;
    private final float gravity = 1f;
    private final float jumpVelocity = 0.25f;
    private double lastFrameTime = 0;
    private final Vector3f FEET = new Vector3f(0, 45, 0);
    private int mineFrameDelay = 0, buildFrameDelay = 0;
    private int wireframeCubeVAO, wireframeCubeVBO, wireShader;
    private final int wireframeVertexCount = 24;
    private int crosshairVao, crosshairVbo, crosshairShader;
    private int crosshairProjLoc;
    private Matrix4f ortho;
    private String blockType = "stone";
    private World world;
    private int invShader;
    private int[] invVaos = new int[9];
    private int[] invVbos = new int[9];
    private int[] invEbos = new int[9];
    private int projUniformLocation, offsetUniformLocation;
    private int[] iconTextures;
    private HotbarSlot[] hotbar = new HotbarSlot[9];
    private final int screen_width = 800, screen_height = 600;
    private Map<String, Integer> blockLookup = new HashMap<>();
    int hotbarSlot = 0;
    private int outlineVao, outlineVbo, outlineEbo, outlineShader;
    private int borderVao, borderVbo, borderEbo, borderShader, borderShader2;
    private int borderOffsetLocation, borderScaleLocation;
    private int barOffsetLocation, barScaleLocation;
    private boolean isInventoryOpen = false;
    private CreativeInventory inventory = new CreativeInventory();
    private int invVertPosition = 0, invHorPosition = 0;
    private boolean selectFromInv = false;
    private int invPage = 1;


    public void run() {
        init();
        loop();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void init() {

        if (!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        window = glfwCreateWindow(screen_width, screen_height, "Minecraft Clone", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        GL.createCapabilities();

        // Camera
        camera = new Camera();

        //WORLD
        world = new World();

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            if (isInventoryOpen) return;
            if (firstMouse) {
                lastX = (float) xpos;
                lastY = (float) ypos;
                firstMouse = false;
            }
            float xoffset = (float) xpos - lastX;
            float yoffset = lastY - (float) ypos;
            lastX = (float) xpos;
            lastY = (float) ypos;

            xoffset *= camera.sensitivity;
            yoffset *= camera.sensitivity;

            camera.yaw += xoffset;
            camera.pitch += yoffset;

            if (camera.pitch > 89.0f) camera.pitch = 89.0f;
            if (camera.pitch < -89.0f) camera.pitch = -89.0f;
        });




        glEnable(GL_DEPTH_TEST);

        String vertexShaderSource = """
        #version 330 core
        layout (location = 0) in vec3 aPos;
        layout (location = 1) in vec2 aTexCoord;
                
        uniform mat4 projection;
        uniform mat4 view;
        uniform mat4 model;
                
        out vec2 texCoord;
                
        void main() {
            gl_Position = projection * view * model * vec4(aPos, 1.0);
            texCoord = aTexCoord;
        }
        """;

        String fragmentShaderSource = """
        #version 330 core
        out vec4 FragColor;
                
        in vec2 texCoord;
                
        uniform sampler2D texture0;
                
        void main() {
            FragColor = texture(texture0, texCoord);
        }
        """;

        shader = ShaderUtils.createShader(vertexShaderSource, fragmentShaderSource);
        glUseProgram(shader);
        int texLoc = glGetUniformLocation(shader, "texture0");
        glUniform1i(texLoc, 0);

        //SetUpStartTime
        lastFrameTime = glfwGetTime();

        // Load the chunk

        for (int x = -5; x <= 5; x++){
            for (int z = -5; z <= 5; z++){
                Chunk chunk = new Chunk(x, z);
                world.addChunk(x, z, chunk);
            }
        }
        int chunkNum = 100;
        for (Chunk chunk : World.chunks.values()) {
            chunk.decorateTrees();
            chunk.chunkMesh = new ChunkMesh(chunk);
        }

        glClearColor(0.2f, 0.4f, 0.9f, 1.0f);
        atlasTexture = TextureLoader.LoadTexture("src/main/resources/TextureAtlas.png"); // Your atlas path here
        camera.position.set(FEET);  // Back away from the cube
        camera.pitch = 0;
        camera.yaw = -90;

        //WIREFRAME RENDER SETUP
        createWireframeCube();

        wireShader = ShaderUtils.createShader(
                // vertex
                """
                #version 330 core
                layout(location=0) in vec3 aPos;
                uniform mat4 projection, view, model;
                void main(){
                  gl_Position = projection * view * model * vec4(aPos,1);
                }
                """,
                // fragment
                """
                #version 330 core
                out vec4 FragColor;
                void main(){
                  FragColor = vec4(1,0,0,1); // bright red
                }
                """
        );

        createCrossHair();

        crosshairShader = ShaderUtils.createShader(
            """ 
            #version 330 core
            layout(location = 0) in vec2 position;
    
            uniform mat4 projection; // orthographic projection
    
            void main() {
                gl_Position = projection * vec4(position, 0.0, 1.0);
            }""",

            """ 
            #version 330 core
            out vec4 FragColor;
    
            void main() {
                FragColor = vec4(1.0); // white color
            }"""
        );

        //CREATE MATRIX FOR RENDERING ON SCREEN
        ortho = new Matrix4f().ortho2D(0, screen_width, 0, screen_height);
        crosshairProjLoc = glGetUniformLocation(crosshairShader, "projection");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                switch (key){
                    case GLFW_KEY_E -> isInventoryOpen = !isInventoryOpen;
                }

                if (isInventoryOpen && gravityStarted == 0){
                    switch (key){
                        case GLFW_KEY_UP -> invVertPosition -= 1;
                        case GLFW_KEY_DOWN -> invVertPosition += 1;
                        case GLFW_KEY_LEFT -> invHorPosition -= 1;
                        case GLFW_KEY_RIGHT -> invHorPosition += 1;
                        case GLFW_KEY_ENTER -> selectFromInv = true;

                        case GLFW_KEY_1 -> invPage = 1;
                        case GLFW_KEY_2 -> invPage = 2;
                    }
                }

                if(isInventoryOpen) return;
                switch (key) {
                    case GLFW_KEY_1 -> hotbarSlot = 0;
                    case GLFW_KEY_2 -> hotbarSlot = 1;
                    case GLFW_KEY_3 -> hotbarSlot = 2;
                    case GLFW_KEY_4 -> hotbarSlot = 3;
                    case GLFW_KEY_5 -> hotbarSlot = 4;
                    case GLFW_KEY_6 -> hotbarSlot = 5;
                    case GLFW_KEY_7 -> hotbarSlot = 6;
                    case GLFW_KEY_8 -> hotbarSlot = 7;
                    case GLFW_KEY_9 -> hotbarSlot = 8;
                }

            }
        });

        glfwSetScrollCallback(window, (windowHandle, xOffset, yOffset) -> {
            if (yOffset > 0) {
                int newSlot = hotbarSlot + (int) yOffset;
                if (newSlot > 8) newSlot = 8;
                hotbarSlot = newSlot;
            }
            else{
                int newSlot = hotbarSlot + (int) yOffset;
                if (newSlot < 0) newSlot = 0;
                hotbarSlot = newSlot;

            }
        });

        invShader = ShaderUtils.createShader(
                """
                #version 330 core
                layout(location = 0) in vec2 position; // 2D screen coords
                layout(location = 1) in vec2 texCoord;
                        
                uniform mat4 projection;
                uniform vec2 offset;
                
                out vec2 vTexCoord;
                        
                void main() {
                    vec2 pos = position + offset;
                    gl_Position = projection * vec4(pos, 0.0, 1.0);
                    vTexCoord = texCoord;
                }""",

                """
                #version 330 core
                in vec2 vTexCoord;
                out vec4 fragColor;
                
                uniform sampler2D tex;
                
                void main() {
                    fragColor = texture(tex, vTexCoord);
                }
                """
        );
        projUniformLocation = glGetUniformLocation(invShader, "projection");
        offsetUniformLocation = glGetUniformLocation(invShader, "offset");

        loadBlockLookup();
        loadIconTextures();
        AssembleHotbar();
        initInvBorder();
        initHotbarOutline();
        System.out.println("Rendering new 'sheep' at 10, 10");
        EntityManager.addEntity(new sheep(0, 40, 0));
    }

    private int vao, vbo, shader;
    int gravityStarted = 3;

    private void loop() {
        glUseProgram(shader);

        int projectionLoc = glGetUniformLocation(shader, "projection");
        int viewLoc = glGetUniformLocation(shader, "view");
        int modelLoc = glGetUniformLocation(shader, "model");

        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(70), (float) screen_width/ screen_height, 0.1f, 100f
        );

        while (!glfwWindowShouldClose(window)) {
            glBindVertexArray(vao);
            velocity.set(0, velocity.y, 0);

            Chunk currentChunk = World.getChunkAtWorld((int) FEET.x, (int) FEET.z);
            if (currentChunk != null) {
                isGrounded = isGroundedMethod(FEET, playerWidth, playerHeight, currentChunk);
            } else {
                isGrounded = false;
            }

            double correctTime = glfwGetTime();
            float deltaTime = (float) (correctTime - lastFrameTime);
            lastFrameTime = correctTime;

            //BLOCK ASSIGNMENT
            blockType = hotbar[hotbarSlot].blockType;

            //Input handler

            float forwardMovement = 0;
            float lateralMovement = 0;

            if (!isInventoryOpen){
                if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) forwardMovement += 1;
                if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) forwardMovement -= 1;
                if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) lateralMovement += 1;
                if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) lateralMovement -= 1;
                if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS && isGrounded) velocity.y = jumpVelocity;
            }
            // Camera movement
            Vector3f direction = new Vector3f(
                    (float) Math.cos(Math.toRadians(camera.yaw)) * (float) Math.cos(Math.toRadians(camera.pitch)),
                    0f,
                    (float) Math.sin(Math.toRadians(camera.yaw)) * (float) Math.cos(Math.toRadians(camera.pitch))
            ).normalize();

            Vector3f lateral = new Vector3f(direction).cross(new Vector3f(0, 1, 0)).normalize();

            float verticalVelocity = velocity.y;
            velocity.x = 0;
            velocity.z = 0;

            //Align direction with cameras and instantiate velocity
            velocity.fma(forwardMovement, direction);
            velocity.fma(lateralMovement, lateral);

            //Add magnitude
            if (velocity.x != 0 || velocity.z != 0){
                if (velocity.lengthSquared() > 0) {
                    Vector3f horizontal = new Vector3f(velocity.x, 0, velocity.z);
                    horizontal.normalize().mul(camera.speed * deltaTime);

                    velocity.x = horizontal.x;
                    velocity.z = horizontal.z;
                }
            }

            velocity.y = verticalVelocity;
            velocity.y -= gravity * deltaTime;
            if (isGrounded && velocity.y < 0) {
                velocity.y = 0;
            }

            if (currentChunk != null) {
                FEET.set(moveWithCollisions(FEET, velocity, currentChunk));
            }
            camera.position.set(FEET.x, FEET.y + 1.3f, FEET.z);

            Matrix4f view = camera.getViewMatrix();


            //BLOCK RAY CASTING AND MINE/PLACE CONTROL CENTRE
            Vector3f playerEyePos = new Vector3f(camera.position.x, camera.position.y, camera.position.z);
            Vector3f viewDir = camera.getViewDirection();  // normalized direction vector

            Raycasthit hit = camera.Raycast(playerEyePos, viewDir, 6.0f, world.getChunkAtWorld((int) playerEyePos.x, (int) playerEyePos.z));

            if(mineFrameDelay > 0){
                mineFrameDelay -= 1;
            }
            if(buildFrameDelay > 0){
                buildFrameDelay -= 1;
            }

            //SHEEP
            EntityManager.updateAll(deltaTime);

            if (hit != null) {
                //System.out.printf("Block hit at %d,%d,%d%n", hit.blockPosition.x, hit.blockPosition.y, hit.blockPosition.z);
                //System.out.println("Hit face normal: " + hit.hitNormal);

                if(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
                    if (mineFrameDelay == 0) {
                        world.removeBlockAt(hit.blockPosition.x, hit.blockPosition.y, hit.blockPosition.z);
                        mineFrameDelay = 20;
                    }
                }
                if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS){
                    if (buildFrameDelay == 0) {
                        int placeX = (int) (hit.blockPosition.x - hit.hitNormal.x);
                        int placeY = (int) (hit.blockPosition.y - hit.hitNormal.y);
                        int placeZ = (int) (hit.blockPosition.z - hit.hitNormal.z);

                        AABB playerBox = new AABB(FEET, playerWidth, playerHeight);
                        AABB blockBox = new AABB(new Vector3f(placeX, placeY, placeZ), 1f, 1f);

                        if (!playerBox.intersects(blockBox)) {
                            World.addBlockAt(placeX, placeY, placeZ, blockType, true);
                        }
                    }
                    buildFrameDelay = 5;
                }



            } else {
                //System.out.println("No block hit");
            }

            if (gravityStarted > 2) {
                velocity.y = -0.1f; // small downward velocity to trigger gravity/fall
                gravityStarted -= 1;
            }

            if (playerEyePos.y < -100){
                glfwSetWindowShouldClose(window, true);
            }

            try (MemoryStack stack = MemoryStack.stackPush()) {
                // Allocate all buffers once here
                FloatBuffer projBuffer = stack.mallocFloat(16);
                FloatBuffer viewBuffer = stack.mallocFloat(16);
                FloatBuffer modelBuffer = stack.mallocFloat(16);

                // Upload projection and view matrices once per frame
                glUniformMatrix4fv(projectionLoc, false, projection.get(projBuffer));
                glUniformMatrix4fv(viewLoc, false, view.get(viewBuffer));

                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                glBindVertexArray(vao);

                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, atlasTexture);

                if (hit != null) {
                    renderWireFrame(hit.blockPosition, shader, modelLoc);
                }

                for (Chunk chunk1 : World.chunks.values()){
                    //System.out.println("Rendering chunk at " + chunk1.chunkX + ", " + chunk1.chunkZ);
                    ChunkMesh mesh = chunk1.chunkMesh;
                    glBindVertexArray(mesh.vao);

                    Matrix4f model = new Matrix4f().translation(chunk1.chunkX * Chunk.CHUNK_SIZE, 0, chunk1.chunkZ * Chunk.CHUNK_SIZE);
                    //System.out.println("Chunk @ " + chunk1.chunkX + ", render at " + (chunk1.chunkX * Chunk.CHUNK_SIZE));
                    model.get(modelBuffer);
                    modelBuffer.rewind();
                    glUniformMatrix4fv(modelLoc, false, modelBuffer);

                    glDrawArrays(GL_TRIANGLES, 0, mesh.vertexCount);
                }

            }

            glUseProgram(crosshairShader);
            glDisable(GL_DEPTH_TEST);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer fb = stack.mallocFloat(16);
                glUniformMatrix4fv(crosshairProjLoc, false, ortho.get(fb));
            }
            if (!isInventoryOpen) {
                glBindVertexArray(crosshairVao);
                glDrawArrays(GL_LINES, 0, 4);
                glBindVertexArray(0);
            }

            RenderHotbarOutline();

            glUseProgram(invShader);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer fb = stack.mallocFloat(16);
                glUniformMatrix4fv(projUniformLocation, false, ortho.get(fb));
            }


            if (isInventoryOpen) invBackground();
            glUseProgram(invShader);

            for (int j = 0; j < hotbar.length; j++){
                glBindVertexArray(invVaos[j]);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, invEbos[j]);
                    float slotX = 95 + j * 70;   // spacing between slots
                    float slotY = 20f;

                    glUniform2f(offsetUniformLocation, slotX, slotY);

                    if (!hotbar[j].blockType.equals("air")) {
                        glBindTexture(GL_TEXTURE_2D, iconTextures[blockLookup.get(hotbar[j].blockType)]);
                        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
                    }

                    //if(!isInventoryOpen)
                        invSlotOutline(j, 520, hotbarSlot == j);

                    glUseProgram(invShader);
            }
            //INVENTORY RENDERING
            if (isInventoryOpen) {

                glUseProgram(invShader);

                int rows = 5;
                int cols = 9;
                float slotSize = 50; // Your slot size
                float padding = 20;
                float totalWidth = cols * (slotSize + padding) - padding;
                float startX = (800 - totalWidth) / 2.0f; // Centered horizontally
                float startY = 200; // Vertical position of top row
                String[][] blockPlaceLogging = new String[rows][cols];
                for (int i = 0; i < rows; i++){
                    blockPlaceLogging[i] = new String[cols];
                }

                for (int row = 0; row < rows; row++) {
                    for (int col = 0; col < cols; col++) {

                        int globalRow = row + rows * (invPage - 1);
                        int globalCol = col;
                        int index = globalRow * cols + globalCol;
                        if (index >= inventory.blockStorage.size()) continue;

                        String slot = inventory.blockStorage.get(index);
                        blockPlaceLogging[rows - row - 1][col] = slot;

                        float x = startX + col * (slotSize + padding);
                        float y = startY + row * (slotSize + padding);
                        glUniform2f(offsetUniformLocation, x, y);

                        glBindVertexArray(invVaos[index % invVaos.length]);
                        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, invEbos[index % invVaos.length]);


                        if (!slot.equals("air")) {
                            //System.out.println(slot);

                            glBindTexture(GL_TEXTURE_2D, iconTextures[blockLookup.get(slot)]);
                            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

                            glFinish();
                        }
                        boolean runHighlightBorder = false;
                        if (invVertPosition < 0 || invVertPosition >= rows) invVertPosition = 0;
                        if (invHorPosition < 0 || invHorPosition >= cols) invHorPosition = 0;
                        //System.out.println("Current row = "+row+" Vertical pos = "+invVertPosition % rows);
                        if(row == invVertPosition && col == invHorPosition) runHighlightBorder = true;
                        invSlotOutline(index % invVaos.length, (row + 1) * (slotSize + padding) - 10, runHighlightBorder);
                        glUseProgram(invShader);
                        //System.out.println(blockPlaceLogging[invVertPosition][invHorPosition]);
                    }
                }
                if (selectFromInv){
                    String newBlock = blockPlaceLogging[invVertPosition][invHorPosition];
                    System.out.println("Picked block = " + newBlock);
                    updateHotbar(newBlock);
                    selectFromInv = false;
                }

            }

            glEnable(GL_DEPTH_TEST);
            glDisable(GL_BLEND);
            glEnable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, 37); // Must match the one used in chunk rendering
            CubeMeshBuilder.renderTexturedCube(0, 10, 0, 1f, "cheese");

            glBindVertexArray(0);
            glDisable(GL_BLEND);
            glUseProgram(shader);

            glfwPollEvents();
            glfwSwapBuffers(window);


        }
        glDeleteProgram(shader);
    }

    public static void main(String[] args) {
        new Main().run();
    }

    public boolean isColliding(Vector3f position, float width, float height, Chunk chunk){
        AABB playerBox = new AABB(position, width, height);
        boolean collision = false;

        int minx = (int) Math.floor(playerBox.min.x);
        int maxx = (int) Math.floor(playerBox.max.x);
        int miny = (int) Math.floor(playerBox.min.y);
        int maxy = (int) Math.floor(playerBox.max.y);
        int minz = (int) Math.floor(playerBox.min.z);
        int maxz = (int) Math.floor(playerBox.max.z);

        for (int x = minx; x <= maxx; x++) {
            for (int y = miny; y <= maxy; y++) {
                for (int z = minz; z <= maxz; z++) {
                    Block block = world.getBlockAt(x, y, z); // world coordinates
                    if (block != null && block.isSolid) {
                        AABB blockBox = new AABB(new Vector3f(x + 0.5f, y, z + 0.5f), 1f, 1f);
                        if (playerBox.intersects(blockBox)) {
                            collision = true;
                        }
                    }
                }
            }
        }
        return collision;
    }

    public boolean isGroundedMethod(Vector3f position, float width, float height, Chunk chunk) {
        Vector3f centerPos = new Vector3f(position.x, position.y, position.z);
        AABB playerBox = new AABB(centerPos, width, height);

        float feetY = playerBox.min.y;
        int miny = (int) Math.floor(playerBox.min.y - 0.5f);

        int minx = (int) Math.floor(playerBox.min.x);
        int maxx = (int) Math.floor(playerBox.max.x);
        int minz = (int) Math.floor(playerBox.min.z);
        int maxz = (int) Math.floor(playerBox.max.z);

        for (int x = minx; x <= maxx; x++) {
            for (int z = minz; z <= maxz; z++) {
                Block block = World.getBlockAt(x, miny, z);
                if (block != null && block.isSolid) {
                    AABB blockBox = new AABB(new Vector3f(x + 0.5f, miny + 0.5f, z + 0.5f), 1f, 1f);
                    if (playerBox.intersects(blockBox)) {
                        float FeetY = playerBox.min.y;
                        float blockTopY = blockBox.max.y;
                        if (Math.abs(FeetY - blockTopY) < 0.5f) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public Vector3f moveWithCollisions(Vector3f position, Vector3f velocity, Chunk chunk){
        Vector3f newPosition = new Vector3f(position);

        //CALC X POSITION AND COLLISIONS
        newPosition.x = newPosition.x + velocity.x;
        if (isColliding(newPosition, playerWidth, playerHeight, chunk)){
            newPosition.x = position.x;
        }

        //CALC Y POSITION AND COLLISIONS
        newPosition.y = newPosition.y + velocity.y;
        if (isColliding(newPosition, playerWidth, playerHeight, chunk)){
            newPosition.y = position.y;
        }

        //CALC Z POSITION AND COLLISIONS
        newPosition.z = newPosition.z + velocity.z;
        if (isColliding(newPosition, playerWidth, playerHeight, chunk)){
            newPosition.z = position.z;
        }
        return newPosition;
    }

    public void renderWireFrame(Vector3i blockPos, int shader, int modelLoc){
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer modelBuffer = stack.mallocFloat(16);

            Matrix4f model = new Matrix4f().translation(blockPos.x, blockPos.y, blockPos.z);
            glUniformMatrix4fv(modelLoc, false, model.get(modelBuffer));
            glBindVertexArray(wireframeCubeVAO);
            glDrawArrays(GL_LINES, 0, wireframeVertexCount);
        }
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    private void createWireframeCube() {
        float[] wireframeVertices = {
                // 12 edges, each edge is 2 points (x,y,z)
                // bottom square
                0, 0, 0,  1, 0, 0,
                1, 0, 0,  1, 0, 1,
                1, 0, 1,  0, 0, 1,
                0, 0, 1,  0, 0, 0,

                // top square
                0, 1, 0,  1, 1, 0,
                1, 1, 0,  1, 1, 1,
                1, 1, 1,  0, 1, 1,
                0, 1, 1,  0, 1, 0,

                // vertical edges
                0, 0, 0,  0, 1, 0,
                1, 0, 0,  1, 1, 0,
                1, 0, 1,  1, 1, 1,
                0, 0, 1,  0, 1, 1,
        };

        wireframeCubeVAO = glGenVertexArrays();
        wireframeCubeVBO = glGenBuffers();

        glBindVertexArray(wireframeCubeVAO);

        glBindBuffer(GL_ARRAY_BUFFER, wireframeCubeVBO);
        glBufferData(GL_ARRAY_BUFFER, wireframeVertices, GL_STATIC_DRAW);

        // position attribute (location = 0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Unbind VAO and VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void createCrossHair(){
        float centerX = screen_width / 2f;
        float centerY = screen_height / 2f;
        float crossHairSize = screen_height / 60f;

        float[] crosshairVertices = new float[] {
                centerX - crossHairSize, centerY, 0f,
                centerX + crossHairSize, centerY, 0f,

                centerX, centerY - crossHairSize, 0f,
                centerX, centerY + crossHairSize, 0f,
        };

        crosshairVao = glGenVertexArrays();
        crosshairVbo = glGenBuffers();

        glBindVertexArray(crosshairVao);
        glBindBuffer(GL_ARRAY_BUFFER, crosshairVbo);
        glBufferData(GL_ARRAY_BUFFER, crosshairVertices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);


    }

    private void createInvSlot(int hotbarPos){

        float offset = (hotbarPos * 50f) + (hotbarPos * 20f) + ((screen_width / 2f) - 305f);
        float[] quadVertices = {
                // positions                     // tex coords
                0f, 0f,                 0f, 0f,
                50f, 0f,            1f, 0f,
                50f, 50f,      1f, 1f,
                0f, 50f,            0f, 1f,
        };


        int[] indices = {
                0, 1, 2,
                2, 3, 0
        };

        int Vao = glGenVertexArrays();
        int Vbo = glGenBuffers();
        int Ebo = glGenBuffers();

        glBindVertexArray(Vao);

        glBindBuffer(GL_ARRAY_BUFFER, Vbo);
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, Ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);


        int stride = 4 * Float.BYTES;

        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);

        invVaos[hotbarPos] = Vao;
        invVbos[hotbarPos] = Vbo;
        invEbos[hotbarPos] = Ebo;
    }

    public void loadIconTextures(){
        iconTextures = new int[90];
        iconTextures[0] = TextureLoader.LoadTexture("src/main/resources/Appicons/bedrock.png");  // bedrock
        iconTextures[1] = TextureLoader.LoadTexture("src/main/resources/Appicons/dirt.png");     // dirt
        iconTextures[2] = TextureLoader.LoadTexture("src/main/resources/Appicons/stone.png");    // stone
        iconTextures[3] = TextureLoader.LoadTexture("src/main/resources/Appicons/grass.png");    // grass
        iconTextures[4] = TextureLoader.LoadTexture("src/main/resources/Appicons/wood.png");     // wood
        iconTextures[5] = TextureLoader.LoadTexture("src/main/resources/Appicons/leaves.png");   // leaves
        iconTextures[6] = TextureLoader.LoadTexture("src/main/resources/Appicons/obsidian.png"); // obsidian
        iconTextures[7] = TextureLoader.LoadTexture("src/main/resources/Appicons/diamond.png");  // diamond
        iconTextures[8] = TextureLoader.LoadTexture("src/main/resources/Appicons/gold.png");
        iconTextures[9] = TextureLoader.LoadTexture("src/main/resources/Appicons/coal.png");  // bedrock
        iconTextures[10] = TextureLoader.LoadTexture("src/main/resources/Appicons/lapis.png");     // dirt
        iconTextures[11] = TextureLoader.LoadTexture("src/main/resources/Appicons/copper.png");    // stone
        iconTextures[12] = TextureLoader.LoadTexture("src/main/resources/Appicons/iron.png");    // grass
        iconTextures[13] = TextureLoader.LoadTexture("src/main/resources/Appicons/redstone.png");     // wood
        iconTextures[14] = TextureLoader.LoadTexture("src/main/resources/Appicons/emerald.png");   // leaves
        iconTextures[15] = TextureLoader.LoadTexture("src/main/resources/Appicons/yellowconcrete.png"); // obsidian
        iconTextures[16] = TextureLoader.LoadTexture("src/main/resources/Appicons/whiteconcrete.png");  // diamond
        iconTextures[17] = TextureLoader.LoadTexture("src/main/resources/Appicons/silverconcrete.png");
        iconTextures[18] = TextureLoader.LoadTexture("src/main/resources/Appicons/redconcrete.png");
        iconTextures[19] = TextureLoader.LoadTexture("src/main/resources/Appicons/purpleconcrete.png");// bedrock
        iconTextures[20] = TextureLoader.LoadTexture("src/main/resources/Appicons/pinkconcrete.png");     // dirt
        iconTextures[21] = TextureLoader.LoadTexture("src/main/resources/Appicons/orangeconcrete.png");    // stone
        iconTextures[22] = TextureLoader.LoadTexture("src/main/resources/Appicons/magentaconcrete.png");    // grass
        iconTextures[23] = TextureLoader.LoadTexture("src/main/resources/Appicons/limeconcrete.png");     // wood
        iconTextures[24] = TextureLoader.LoadTexture("src/main/resources/Appicons/lightblueconcrete.png");   // leaves
        iconTextures[25] = TextureLoader.LoadTexture("src/main/resources/Appicons/greenconcrete.png"); // obsidian
        iconTextures[26] = TextureLoader.LoadTexture("src/main/resources/Appicons/greyconcrete.png");  // diamond
        iconTextures[27] = TextureLoader.LoadTexture("src/main/resources/Appicons/cyanconcrete.png");
        iconTextures[28] = TextureLoader.LoadTexture("src/main/resources/Appicons/brownconcrete.png");  // bedrock
        iconTextures[29] = TextureLoader.LoadTexture("src/main/resources/Appicons/blueconcrete.png");     // dirt
        iconTextures[30] = TextureLoader.LoadTexture("src/main/resources/Appicons/blackconcrete.png");    // stone
        iconTextures[31] = TextureLoader.LoadTexture("src/main/resources/Appicons/netherbrick.png");    // grass
        iconTextures[32] = TextureLoader.LoadTexture("src/main/resources/Appicons/brick.png");     // wood
        iconTextures[33] = TextureLoader.LoadTexture("src/main/resources/Appicons/prismarine.png");   // leaves
        iconTextures[34] = TextureLoader.LoadTexture("src/main/resources/Appicons/darkprismarine.png"); // obsidian
        iconTextures[35] = TextureLoader.LoadTexture("src/main/resources/Appicons/prismarinebrick.png");  // diamond
        iconTextures[36] = TextureLoader.LoadTexture("src/main/resources/Appicons/stonebrick.png");
        iconTextures[37] = TextureLoader.LoadTexture("src/main/resources/Appicons/cheese.png");  // bedrock
        iconTextures[38] = TextureLoader.LoadTexture("src/main/resources/Appicons/sadobsidian.png");     // dirt
        iconTextures[39] = TextureLoader.LoadTexture("src/main/resources/Appicons/cobblestone.png");    // stone
        iconTextures[40] = TextureLoader.LoadTexture("src/main/resources/Appicons/amethyst.png");    // grass
        iconTextures[41] = TextureLoader.LoadTexture("src/main/resources/Appicons/portal.png");     // wood
        iconTextures[42] = TextureLoader.LoadTexture("src/main/resources/Appicons/diamondblock.png");   // leaves
        iconTextures[43] = TextureLoader.LoadTexture("src/main/resources/Appicons/calcite.png"); // obsidian
        iconTextures[44] = TextureLoader.LoadTexture("src/main/resources/Appicons/white.png");  // diamond
        iconTextures[45] = TextureLoader.LoadTexture("src/main/resources/Appicons/birch.png");  // bedrock
        iconTextures[46] = TextureLoader.LoadTexture("src/main/resources/Appicons/spruce.png");     // dirt
        iconTextures[47] = TextureLoader.LoadTexture("src/main/resources/Appicons/jungle.png");    // stone
        iconTextures[48] = TextureLoader.LoadTexture("src/main/resources/Appicons/darkoak.png");    // grass
        iconTextures[49] = TextureLoader.LoadTexture("src/main/resources/Appicons/acacia.png");     // wood
        iconTextures[50] = TextureLoader.LoadTexture("src/main/resources/Appicons/cherry.png");   // leaves
        iconTextures[51] = TextureLoader.LoadTexture("src/main/resources/Appicons/mangrove.png"); // obsidian
        iconTextures[52] = TextureLoader.LoadTexture("src/main/resources/Appicons/planks.png");  // diamond
        iconTextures[53] = TextureLoader.LoadTexture("src/main/resources/Appicons/birchplanks.png");
        iconTextures[54] = TextureLoader.LoadTexture("src/main/resources/Appicons/spruceplanks.png");  // bedrock
        iconTextures[55] = TextureLoader.LoadTexture("src/main/resources/Appicons/jungleplanks.png");     // dirt
        iconTextures[56] = TextureLoader.LoadTexture("src/main/resources/Appicons/darkoakplanks.png");    // stone
        iconTextures[57] = TextureLoader.LoadTexture("src/main/resources/Appicons/acaciaplanks.png");    // grass
        iconTextures[58] = TextureLoader.LoadTexture("src/main/resources/Appicons/cherryplanks.png");     // wood
        iconTextures[59] = TextureLoader.LoadTexture("src/main/resources/Appicons/mangroveplanks.png");   // leaves
        iconTextures[60] = TextureLoader.LoadTexture("src/main/resources/Appicons/tuff.png"); // obsidian
        iconTextures[61] = TextureLoader.LoadTexture("src/main/resources/Appicons/tuffbricks.png");  // diamond
        iconTextures[62] = TextureLoader.LoadTexture("src/main/resources/Appicons/deepslate.png");
        iconTextures[63] = TextureLoader.LoadTexture("src/main/resources/Appicons/deepslatebricks.png");
        iconTextures[64] = TextureLoader.LoadTexture("src/main/resources/Appicons/copperblock.png");// bedrock
        iconTextures[65] = TextureLoader.LoadTexture("src/main/resources/Appicons/ironblock.png");     // dirt
        iconTextures[66] = TextureLoader.LoadTexture("src/main/resources/Appicons/goldblock.png");    // stone
        iconTextures[67] = TextureLoader.LoadTexture("src/main/resources/Appicons/emeraldblock.png");    // grass
        iconTextures[68] = TextureLoader.LoadTexture("src/main/resources/Appicons/lapisblock.png");     // wood
        iconTextures[69] = TextureLoader.LoadTexture("src/main/resources/Appicons/redstoneblock.png");   // leaves
        iconTextures[70] = TextureLoader.LoadTexture("src/main/resources/Appicons/coalblock.png"); // obsidian
        iconTextures[71] = TextureLoader.LoadTexture("src/main/resources/Appicons/basalt.png");  // diamond
        iconTextures[72] = TextureLoader.LoadTexture("src/main/resources/Appicons/bone.png");
        iconTextures[73] = TextureLoader.LoadTexture("src/main/resources/Appicons/sand.png");  // bedrock
        iconTextures[74] = TextureLoader.LoadTexture("src/main/resources/Appicons/gravel.png");     // dirt
        iconTextures[75] = TextureLoader.LoadTexture("src/main/resources/Appicons/ice.png");    // stone
        iconTextures[76] = TextureLoader.LoadTexture("src/main/resources/Appicons/packedice.png");    // grass
        iconTextures[77] = TextureLoader.LoadTexture("src/main/resources/Appicons/blueice.png");     // wood
        iconTextures[78] = TextureLoader.LoadTexture("src/main/resources/Appicons/craftingtable.png");   // leaves
        iconTextures[79] = TextureLoader.LoadTexture("src/main/resources/Appicons/mud.png"); // obsidian
        iconTextures[80] = TextureLoader.LoadTexture("src/main/resources/Appicons/quartz.png");  // diamond
        iconTextures[81] = TextureLoader.LoadTexture("src/main/resources/Appicons/quartzbrick.png");
        iconTextures[82] = TextureLoader.LoadTexture("src/main/resources/Appicons/cheesebrick.png");  // bedrock
        iconTextures[83] = TextureLoader.LoadTexture("src/main/resources/Appicons/yellowcoral.png");     // dirt
        iconTextures[84] = TextureLoader.LoadTexture("src/main/resources/Appicons/redcoral.png");    // stone
        iconTextures[85] = TextureLoader.LoadTexture("src/main/resources/Appicons/purplecoral.png");    // grass
        iconTextures[86] = TextureLoader.LoadTexture("src/main/resources/Appicons/pinkcoral.png");     // wood
        iconTextures[87] = TextureLoader.LoadTexture("src/main/resources/Appicons/bluecoral.png");   // leaves
        iconTextures[88] = TextureLoader.LoadTexture("src/main/resources/Appicons/mossycobblestone.png"); // obsidian
        iconTextures[89] = TextureLoader.LoadTexture("src/main/resources/Appicons/netherrack.png");  // diamond
    }

    public void loadBlockLookup(){
        blockLookup.put("air", -1);
        blockLookup.put("bedrock", 0);
        blockLookup.put("dirt", 1);
        blockLookup.put("stone", 2);
        blockLookup.put("grass", 3);
        blockLookup.put("wood", 4);
        blockLookup.put("leaves", 5);
        blockLookup.put("obsidian", 6);
        blockLookup.put("diamond", 7);
        blockLookup.put("gold", 8);

        blockLookup.put("coal", 9);
        blockLookup.put("lapis", 10);
        blockLookup.put("copper", 11);
        blockLookup.put("iron", 12);
        blockLookup.put("redstone", 13);
        blockLookup.put("emerald", 14);
        blockLookup.put("yellowconcrete", 15);
        blockLookup.put("whiteconcrete", 16);
        blockLookup.put("silverconcrete", 17);

        blockLookup.put("redconcrete", 18);
        blockLookup.put("purpleconcrete", 19);
        blockLookup.put("pinkconcrete", 20);
        blockLookup.put("orangeconcrete", 21);
        blockLookup.put("magentaconcrete", 22);
        blockLookup.put("limeconcrete", 23);
        blockLookup.put("lightblueconcrete", 24);
        blockLookup.put("greenconcrete", 25);
        blockLookup.put("greyconcrete", 26);

        blockLookup.put("cyanconcrete", 27);
        blockLookup.put("brownconcrete", 28);
        blockLookup.put("blueconcrete", 29);
        blockLookup.put("blackconcrete", 30);
        blockLookup.put("netherbrick", 31);
        blockLookup.put("brick", 32);
        blockLookup.put("prismarine", 33);
        blockLookup.put("darkprismarine", 34);
        blockLookup.put("prismarinebrick", 35);

        blockLookup.put("stonebrick", 36);
        blockLookup.put("cheese", 37);
        blockLookup.put("sadobsidian", 38);
        blockLookup.put("cobblestone", 39);
        blockLookup.put("amethyst", 40);
        blockLookup.put("portal", 41);
        blockLookup.put("diamondblock", 42);
        blockLookup.put("calcite", 43);
        blockLookup.put("white", 44);

        blockLookup.put("birch", 45);
        blockLookup.put("spruce", 46);
        blockLookup.put("jungle", 47);
        blockLookup.put("darkoak", 48);
        blockLookup.put("acacia", 49);
        blockLookup.put("cherry", 50);
        blockLookup.put("mangrove", 51);
        blockLookup.put("planks", 52);
        blockLookup.put("birchplanks", 53);

        blockLookup.put("spruceplanks", 54);
        blockLookup.put("jungleplanks", 55);
        blockLookup.put("darkoakplanks", 56);
        blockLookup.put("acaciaplanks", 57);
        blockLookup.put("cherryplanks", 58);
        blockLookup.put("mangroveplanks", 59);
        blockLookup.put("tuff", 60);
        blockLookup.put("tuffbricks", 61);
        blockLookup.put("deepslate", 62);

        blockLookup.put("deepslatebricks", 63);
        blockLookup.put("copperblock", 64);
        blockLookup.put("ironblock", 65);
        blockLookup.put("goldblock", 66);
        blockLookup.put("emeraldblock", 67);
        blockLookup.put("lapisblock", 68);
        blockLookup.put("redstoneblock", 69);
        blockLookup.put("coalblock", 70);
        blockLookup.put("basalt", 71);

        blockLookup.put("bone", 72);
        blockLookup.put("sand", 73);
        blockLookup.put("gravel", 74);
        blockLookup.put("ice", 75);
        blockLookup.put("packedice", 76);
        blockLookup.put("blueice", 77);
        blockLookup.put("craftingtable", 78);
        blockLookup.put("mud", 79);
        blockLookup.put("quartz", 80);

        blockLookup.put("quartzbrick", 81);
        blockLookup.put("cheesebrick", 82);
        blockLookup.put("yellowcoral", 83);
        blockLookup.put("redcoral", 84);
        blockLookup.put("purplecoral", 85);
        blockLookup.put("pinkcoral", 86);
        blockLookup.put("bluecoral", 87);
        blockLookup.put("mossycobblestone", 88);
        blockLookup.put("netherrack", 89);

    }

    public void AssembleHotbar(){
        hotbar[0] = new HotbarSlot("stone", 64);
        createInvSlot(0);
        hotbar[1] = new HotbarSlot("dirt", 64);
        createInvSlot(1);
        hotbar[2] = new HotbarSlot("grass", 64);
        createInvSlot(2);
        hotbar[3] = new HotbarSlot("wood", 64);
        createInvSlot(3);
        hotbar[4] = new HotbarSlot("leaves", 64);
        createInvSlot(4);
        hotbar[5] = new HotbarSlot("diamond", 64);
        createInvSlot(5);
        hotbar[6] = new HotbarSlot("gold", 64);
        createInvSlot(6);
        hotbar[7] = new HotbarSlot("obsidian", 64);
        createInvSlot(7);
        hotbar[8] = new HotbarSlot("bedrock", 64);
        createInvSlot(8);
    }

    public void initHotbarOutline(){
        float[] vertices = {
                0f, 0f,
                1f, 0f,
                1f, 1f,
                0f, 1f
        };

        int[] indexes = {
                0, 1, 2,
                2, 3, 0
        };

        outlineShader = ShaderUtils.createShader(
                """
                        #version 330 core
                        
                        layout(location = 0) in vec2 aPos;
                        
                        uniform vec2 uOffset;  // screen position offset
                        uniform vec2 uScale;   // scale in pixels
                        
                        out vec4 vertexColor;
                        
                        void main() {
                            vec2 pos = aPos * uScale + uOffset;
                            vec2 normalizedPos = pos / vec2(800.0, 600.0) * 2.0 - 1.0;
                            gl_Position = vec4(normalizedPos.x, -normalizedPos.y, 0.0, 1.0);
                        }
                        """,

                """
                        #version 330 core
                        out vec4 FragColor;
                        void main() {
                            FragColor = vec4(0.1, 0.1, 0.1, 0.6); // black
                        }
                        
                        """
        );

        outlineVao = glGenVertexArrays();
        outlineVbo = glGenBuffers();
        outlineEbo = glGenBuffers();

        barOffsetLocation = glGetUniformLocation(outlineShader, "uOffset");
        barScaleLocation = glGetUniformLocation(outlineShader, "uScale");

        glBindVertexArray(outlineVao);
        glBindBuffer(GL_ARRAY_BUFFER, outlineVbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, outlineEbo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexes, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
    }

    public void RenderHotbarOutline(){
        glUseProgram(outlineShader);

        glUniform2f(barOffsetLocation, 85f, 520f);
        glUniform2f(barScaleLocation, 630f, 70f);

        glBindVertexArray(outlineVao);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
        glUseProgram(0);
    }

    public void invSlotOutline(int slotOffset, float Ypos, boolean doSelection){

        //borderOffsetLocation = offsetUniformLocation;

        if(doSelection) glUseProgram(borderShader2);
        else glUseProgram(borderShader);

        glUniform2f(borderOffsetLocation, 85f + (slotOffset * 70f), Ypos);
        glUniform2f(borderScaleLocation, 70f, 5f);

        glBindVertexArray(borderVao);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        glUniform2f(borderOffsetLocation, 85f + (slotOffset * 70f), Ypos + 65);
        glUniform2f(borderScaleLocation, 70f, 5f);

        glBindVertexArray(borderVao);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        glUniform2f(borderOffsetLocation, 85f + (slotOffset * 70f), Ypos + 5);
        glUniform2f(borderScaleLocation, 5f, 60f);

        glBindVertexArray(borderVao);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        glUniform2f(borderOffsetLocation, 150f + (slotOffset * 70f), Ypos + 5);
        glUniform2f(borderScaleLocation, 5f, 60f);

        glBindVertexArray(borderVao);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
        glUseProgram(0);


    }

    public void initInvBorder(){
        float[] vertices = {
                0f, 0f,
                1f, 0f,
                1f, 1f,
                0f, 1f
        };

        int[] indexes = {
                0, 1, 2,
                2, 3, 0
        };



        borderShader = ShaderUtils.createShader(
                """
                        #version 330 core
                        
                        layout(location = 0) in vec2 aPos;
                        
                        uniform vec2 uOffset;  // screen position offset
                        uniform vec2 uScale;   // scale in pixels
                        
                        out vec4 vertexColor;
                        
                        void main() {
                            vec2 pos = aPos * uScale + uOffset;
                            vec2 normalizedPos = pos / vec2(800.0, 600.0) * 2.0 - 1.0;
                            gl_Position = vec4(normalizedPos.x, -normalizedPos.y, 0.0, 1.0);
                        }
                        """,

                """
                        #version 330 core
                        out vec4 FragColor;
                        void main() {
                            FragColor = vec4(0.8, 0.8, 0.8, 0.4); // black
                        }
                        
                        """
        );
        borderShader2 = ShaderUtils.createShader(
                """
                        #version 330 core
                        
                        layout(location = 0) in vec2 aPos;
                        
                        uniform vec2 uOffset;  // screen position offset
                        uniform vec2 uScale;   // scale in pixels
                        
                        out vec4 vertexColor;
                        
                        void main() {
                            vec2 pos = aPos * uScale + uOffset;
                            vec2 normalizedPos = pos / vec2(800.0, 600.0) * 2.0 - 1.0;
                            gl_Position = vec4(normalizedPos.x, -normalizedPos.y, 0.0, 1.0);
                        }
                        """,

                """
                        #version 330 core
                        out vec4 FragColor;
                        void main() {
                            FragColor = vec4(0.8, 0.8, 0.8, 0.8); // black
                        }
                        
                        """
        );



        borderVao = glGenVertexArrays();
        borderVbo = glGenBuffers();
        borderEbo = glGenBuffers();

        borderOffsetLocation = glGetUniformLocation(borderShader, "uOffset");
        borderScaleLocation = glGetUniformLocation(borderShader, "uScale");

        glBindVertexArray(borderVao);
        glBindBuffer(GL_ARRAY_BUFFER, borderVbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, borderEbo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexes, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
    }

    public void invBackground(){
        if (isInventoryOpen) {
            glUseProgram(outlineShader);

            glUniform2f(barOffsetLocation, 0f, 0f);
            glUniform2f(barScaleLocation, (float) screen_width, (float) screen_height);

            glBindVertexArray(outlineVao);
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

            glBindVertexArray(0);
            glUseProgram(0);
        }
    }

    public void updateHotbar(String newType){
        for (int i = 8; i >= 1; i--){
            HotbarSlot tempSlot = hotbar[i-1];
            hotbar[i] = tempSlot;
        }
        HotbarSlot newSlot = new HotbarSlot(newType, 64);
        hotbar[0] = newSlot;
    }
}
