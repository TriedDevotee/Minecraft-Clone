package org.example.WorldRendering;
import static org.lwjgl.opengl.GL20.*;


public class ShaderUtils {
    public static int createShader(String vertexCode, String fragmentCode){
        int vertex = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertex, vertexCode);
        glCompileShader(vertex);
        if (glGetShaderi(vertex, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Vertex Shader compilation failed:");
            System.err.println(glGetShaderInfoLog(vertex));
        }

        int fragment = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragment, fragmentCode);
        glCompileShader(fragment);
        if (glGetShaderi(fragment, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Fragment Shader compilation failed:");
            System.err.println(glGetShaderInfoLog(fragment));
        }

        int program = glCreateProgram();
        glAttachShader(program, vertex);
        glAttachShader(program, fragment);
        glLinkProgram(program);

        glDeleteShader(vertex);
        glDeleteShader(fragment);

        return program;
    }
}
