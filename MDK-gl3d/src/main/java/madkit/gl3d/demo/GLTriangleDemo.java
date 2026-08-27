package madkit.gl3d.demo;

import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL20C.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20C.glAttachShader;
import static org.lwjgl.opengl.GL20C.glCompileShader;
import static org.lwjgl.opengl.GL20C.glCreateProgram;
import static org.lwjgl.opengl.GL20C.glCreateShader;
import static org.lwjgl.opengl.GL20C.glDeleteShader;
import static org.lwjgl.opengl.GL20C.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20C.glGetProgrami;
import static org.lwjgl.opengl.GL20C.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20C.glGetShaderi;
import static org.lwjgl.opengl.GL20C.glLinkProgram;
import static org.lwjgl.opengl.GL20C.glShaderSource;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL15C.glGenBuffers;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11C;

import madkit.gl3d.GLWindow;

/** A minimal OpenGL 3.3 smoke test for the MDK-gl3d module. */
public final class GLTriangleDemo {
    private static final String VERTEX_SHADER = "#version 330 core\n"
            + "layout (location = 0) in vec2 position;\n"
            + "void main() { gl_Position = vec4(position, 0.0, 1.0); }\n";
    private static final String FRAGMENT_SHADER = "#version 330 core\n"
            + "out vec4 color;\n"
            + "void main() { color = vec4(0.12, 0.65, 0.95, 1.0); }\n";

    private GLTriangleDemo() {
    }

    public static void main(String[] args) {
        GLWindow[] holder = new GLWindow[1];
        holder[0] = new GLWindow("MaDKit GL3D — Hello Triangle", 1_024, 640, true,
                new TriangleRenderer()::render);
        try {
            holder[0].start();
            while (holder[0].isStarted() && !Thread.currentThread().isInterrupted()) {
                Thread.sleep(100);
                if (holder[0].handle() == 0) {
                    break;
                }
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } finally {
            holder[0].close();
        }
    }

    private static final class TriangleRenderer {
        private int program;
        private int vao;
        private boolean initialized;

        void render() {
            if (!initialized) {
                initialized = true;
                initialize();
            }
            glClearColor(0.035f, 0.045f, 0.08f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);
            glUseProgram(program);
            glBindVertexArray(vao);
            glDrawArrays(GL_TRIANGLES, 0, 3);
        }

        private void initialize() {
            program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
            vao = glGenVertexArrays();
            int buffer = glGenBuffers();
            FloatBuffer vertices = BufferUtils.createFloatBuffer(6).put(new float[] { -0.7f, -0.6f, 0.7f, -0.6f, 0.0f, 0.7f });
            vertices.flip();
            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, buffer);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);
        }

        private static int createProgram(String vertexSource, String fragmentSource) {
            int vertex = compile(GL_VERTEX_SHADER, vertexSource);
            int fragment = compile(GL_FRAGMENT_SHADER, fragmentSource);
            int result = glCreateProgram();
            glAttachShader(result, vertex);
            glAttachShader(result, fragment);
            glLinkProgram(result);
            glDeleteShader(vertex);
            glDeleteShader(fragment);
            if (glGetProgrami(result, GL_LINK_STATUS) == GL11C.GL_FALSE) {
                throw new IllegalStateException("Shader link failed: " + glGetProgramInfoLog(result));
            }
            return result;
        }

        private static int compile(int type, String source) {
            int shader = glCreateShader(type);
            glShaderSource(shader, source);
            glCompileShader(shader);
            if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL11C.GL_FALSE) {
                throw new IllegalStateException("Shader compilation failed: " + glGetShaderInfoLog(shader));
            }
            return shader;
        }
    }
}
