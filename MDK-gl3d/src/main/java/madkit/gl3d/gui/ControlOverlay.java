package madkit.gl3d.gui;

import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glDepthMask;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL15C.glGenBuffers;
import static org.lwjgl.opengl.GL20C.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20C.glAttachShader;
import static org.lwjgl.opengl.GL20C.glCompileShader;
import static org.lwjgl.opengl.GL20C.glCreateProgram;
import static org.lwjgl.opengl.GL20C.glCreateShader;
import static org.lwjgl.opengl.GL20C.glDeleteProgram;
import static org.lwjgl.opengl.GL20C.glDeleteShader;
import static org.lwjgl.opengl.GL20C.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20C.glGetProgrami;
import static org.lwjgl.opengl.GL20C.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20C.glGetShaderi;
import static org.lwjgl.opengl.GL20C.glGetUniformLocation;
import static org.lwjgl.opengl.GL20C.glShaderSource;
import static org.lwjgl.opengl.GL20C.glUniform2f;
import static org.lwjgl.opengl.GL20C.glUniform4f;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glLinkProgram;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

/** Renders the GLFW-native button surface on the OpenGL thread. */
public final class ControlOverlay implements AutoCloseable {
    private static final String VERTEX = "#version 330 core\n"
            + "layout (location = 0) in vec2 position;\n"
            + "uniform vec2 viewport;\n"
            + "void main() { vec2 p = position / viewport * 2.0 - 1.0; p.y = -p.y; gl_Position = vec4(p, 0.0, 1.0); }\n";
    private static final String FRAGMENT = "#version 330 core\n"
            + "uniform vec4 buttonColor;\n"
            + "out vec4 color;\n"
            + "void main() { color = buttonColor; }\n";

    private final SimulationControlPanel controls;
    private int program;
    private int vao;
    private int buffer;
    private int viewportLocation;
    private int colorLocation;
    private boolean initialized;
    private FloatBuffer vertices;

    public ControlOverlay(SimulationControlPanel controls) {
        this.controls = controls;
    }

    /** Renders button backgrounds and labels using the current GL context. */
    public void render(int width, int height) {
        render(width, height, null, "");
    }

    /**
     * Renders the controls plus optional status and help text.
     *
     * @param width framebuffer width in pixels
     * @param height framebuffer height in pixels
     * @param status current simulation status, or {@code null}
     * @param help keyboard and mouse help text
     */
    public void render(int width, int height, SimulationStatus status, String help) {
        if (width <= 0 || height <= 0) return;
        if (!initialized) initialize();
        // The overlay is a screen-space pass and must not compete with scene depth.
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        try {
            glUseProgram(program);
            glUniform2f(viewportLocation, width, height);
            glBindVertexArray(vao);
            for (SimulationControlPanel.Button button : controls.buttons()) {
                if (button.width() <= 0 || button.height() <= 0) continue;
                setColor(controls.isEnabled(button) ? 0.12f : 0.08f,
                        controls.isEnabled(button) ? 0.55f : 0.08f, 0.18f, 0.88f);
                drawQuad(button.x(), button.y(), button.width(), button.height());
                drawButtonLabel(button);
            }
            if (status != null) {
                setColor(0.95f, 0.85f, 0.35f, 1f);
                drawText("STATE: " + status.state() + " TICK: " + status.tick()
                        + " BEES: " + status.beeCount(), 10, 126, 2, 1f, 0.85f, 0.35f, 1f);
                drawText("FOOD: " + format(status.foodStore()) + " SPEED: "
                        + format(status.simulationSpeed()) + " FPS: " + format(status.renderFps())
                        + " FRAME: " + format(status.renderFrameMicros()) + "us GPU: "
                        + format(status.gpuFrameMicros()) + "us",
                        10, 144, 2, 0.95f, 0.85f, 0.35f, 1f);
                drawText("HIVE: " + count(status, madkit.gl3d.bees.BeeState.IN_HIVE)
                        + " SEARCH: " + count(status, madkit.gl3d.bees.BeeState.SEARCHING)
                        + " COLLECT: " + count(status, madkit.gl3d.bees.BeeState.COLLECTING)
                        + " RETURN: " + count(status, madkit.gl3d.bees.BeeState.RETURNING)
                        + " REST: " + count(status, madkit.gl3d.bees.BeeState.RESTING),
                        10, 162, 2, 0.95f, 0.85f, 0.35f, 1f);
                drawText("TIME: " + format(status.simulatedTimeSeconds()) + "s TICK: "
                        + format(status.simulationTickMicros()) + "us", 10, 180, 2,
                        0.95f, 0.85f, 0.35f, 1f);
            }
            if (controls.labels()) {
                setColor(0.55f, 0.9f, 0.95f, 1f);
                drawText("HIVE ORIGIN  FLOWER FIELD  BEE INSTANCES", 10, 108, 2,
                        0.55f, 0.9f, 0.95f, 1f);
            }
            if (help != null && !help.isBlank()) {
                setColor(0.75f, 0.82f, 0.9f, 1f);
                drawText(help, 10, 198, 2, 0.75f, 0.82f, 0.9f, 1f);
            }
        } finally {
            glDepthMask(true);
        }
    }

    private void setColor(float red, float green, float blue, float alpha) {
        glUniform4f(colorLocation, red, green, blue, alpha);
    }

    private void drawQuad(double x, double y, double width, double height) {
        vertices.clear();
        vertices.put((float) x).put((float) y).put((float) (x + width)).put((float) y)
                .put((float) (x + width)).put((float) (y + height)).put((float) x).put((float) y)
                .put((float) (x + width)).put((float) (y + height)).put((float) x).put((float) (y + height)).flip();
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

    private void drawText(String text, double x, double y, int scale,
            float red, float green, float blue, float alpha) {
        setColor(red, green, blue, alpha);
        vertices.clear();
        double cursor = x;
        for (char character : text.toUpperCase().toCharArray()) {
            String[] glyph = glyph(character);
            for (int row = 0; row < glyph.length; row++) {
                for (int column = 0; column < glyph[row].length(); column++) {
                    if (glyph[row].charAt(column) == '#') {
                        appendQuad(cursor + column * scale, y + row * scale, scale, scale);
                    }
                }
            }
            cursor += 6d * scale;
        }
        int vertexCount = vertices.position() / 2;
        if (vertexCount == 0) return;
        vertices.flip();
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
    }

    private void appendQuad(double x, double y, double width, double height) {
        vertices.put((float) x).put((float) y).put((float) (x + width)).put((float) y)
                .put((float) (x + width)).put((float) (y + height)).put((float) x).put((float) y)
                .put((float) (x + width)).put((float) (y + height)).put((float) x).put((float) (y + height));
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static int count(SimulationStatus status, madkit.gl3d.bees.BeeState state) {
        return status.stateCounts().getOrDefault(state, 0);
    }

    private static String[] glyph(char character) {
        return switch (character) {
            case 'A' -> new String[] { ".###.", "#...#", "#...#", "#####", "#...#", "#...#", "#...#" };
            case 'B' -> new String[] { "####.", "#...#", "#...#", "####.", "#...#", "#...#", "####." };
            case 'C' -> new String[] { ".####", "#....", "#....", "#....", "#....", "#....", ".####" };
            case 'D' -> new String[] { "####.", "#...#", "#...#", "#...#", "#...#", "#...#", "####." };
            case 'E' -> new String[] { "#####", "#....", "#....", "####.", "#....", "#....", "#####" };
            case 'F' -> new String[] { "#####", "#....", "#....", "####.", "#....", "#....", "#...." };
            case 'G' -> new String[] { ".####", "#....", "#....", "#.###", "#...#", "#...#", ".####" };
            case 'H' -> new String[] { "#...#", "#...#", "#...#", "#####", "#...#", "#...#", "#...#" };
            case 'I' -> new String[] { "#####", "..#..", "..#..", "..#..", "..#..", "..#..", "#####" };
            case 'K' -> new String[] { "#...#", "#..#.", "#.#..", "##...", "#.#..", "#..#.", "#...#" };
            case 'L' -> new String[] { "#....", "#....", "#....", "#....", "#....", "#....", "#####" };
            case 'M' -> new String[] { "#...#", "##.##", "#.#.#", "#.#.#", "#...#", "#...#", "#...#" };
            case 'N' -> new String[] { "#...#", "##..#", "##..#", "#.#.#", "#..##", "#..##", "#...#" };
            case 'O' -> new String[] { ".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###." };
            case 'P' -> new String[] { "####.", "#...#", "#...#", "####.", "#....", "#....", "#...." };
            case 'R' -> new String[] { "####.", "#...#", "#...#", "####.", "#.#..", "#..#.", "#...#" };
            case 'S' -> new String[] { ".####", "#....", "#....", ".###.", "....#", "....#", "####." };
            case 'T' -> new String[] { "#####", "..#..", "..#..", "..#..", "..#..", "..#..", "..#.." };
            case 'U' -> new String[] { "#...#", "#...#", "#...#", "#...#", "#...#", "#...#", ".###." };
            case 'V' -> new String[] { "#...#", "#...#", "#...#", "#...#", "#...#", ".#.#.", "..#.." };
            case 'W' -> new String[] { "#...#", "#...#", "#...#", "#.#.#", "#.#.#", "##.##", "#...#" };
            case 'Y' -> new String[] { "#...#", "#...#", ".#.#.", "..#..", "..#..", "..#..", "..#.." };
            case '0' -> new String[] { ".###.", "#...#", "#..##", "#.#.#", "##..#", "#...#", ".###." };
            case '1' -> new String[] { "..#..", ".##..", "..#..", "..#..", "..#..", "..#..", ".###." };
            case '2' -> new String[] { ".###.", "#...#", "....#", "...#.", "..#..", ".#...", "#####" };
            case '3' -> new String[] { "####.", "....#", "....#", ".###.", "....#", "....#", "####." };
            case '4' -> new String[] { "...#.", "..##.", ".#.#.", "#..#.", "#####", "...#.", "...#." };
            case '5' -> new String[] { "#####", "#....", "#....", "" + ".###.", "....#", "....#", "####." };
            case '6' -> new String[] { ".###.", "#....", "#....", "####.", "#...#", "#...#", ".###." };
            case '7' -> new String[] { "#####", "....#", "...#.", "..#..", ".#...", ".#...", ".#..." };
            case '8' -> new String[] { ".###.", "#...#", "#...#", ".###.", "#...#", "#...#", ".###." };
            case '9' -> new String[] { ".###.", "#...#", "#...#", ".####", "....#", "....#", ".###." };
            case ':' -> new String[] { ".....", "..#..", ".....", ".....", "..#..", ".....", "....." };
            case '.' -> new String[] { ".....", ".....", ".....", ".....", ".....", ".....", "..#.." };
            case '-' -> new String[] { ".....", ".....", ".....", ".###.", ".....", ".....", "....." };
            case '/' -> new String[] { "....#", "...#.", "...#.", "..#..", ".#...", ".#...", "#...." };
            case '|' -> new String[] { "..#..", "..#..", "..#..", "..#..", "..#..", "..#..", "..#.." };
            case '+' -> new String[] { ".....", "..#..", "..#..", "#####", "..#..", "..#..", "....." };
            default -> new String[] { ".....", ".....", ".....", ".....", ".....", ".....", "....." };
        };
    }

    private void initialize() {
        program = createProgram();
        viewportLocation = glGetUniformLocation(program, "viewport");
        colorLocation = glGetUniformLocation(program, "buttonColor");
        vao = glGenVertexArrays();
        buffer = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glVertexAttribPointer(0, 2, org.lwjgl.opengl.GL11C.GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        vertices = BufferUtils.createFloatBuffer(100_000);
        initialized = true;
    }

    private static int createProgram() {
        int vertex = compile(GL_VERTEX_SHADER, VERTEX);
        int fragment = compile(GL_FRAGMENT_SHADER, FRAGMENT);
        int result = glCreateProgram();
        glAttachShader(result, vertex);
        glAttachShader(result, fragment);
        glLinkProgram(result);
        glDeleteShader(vertex);
        glDeleteShader(fragment);
        if (glGetProgrami(result, GL_LINK_STATUS) == 0) throw new IllegalStateException(glGetProgramInfoLog(result));
        return result;
    }

    private static int compile(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) throw new IllegalStateException(glGetShaderInfoLog(shader));
        return shader;
    }

    @Override
    public void close() {
        if (!initialized) return;
        glDeleteBuffers(buffer);
        glDeleteVertexArrays(vao);
        glDeleteProgram(program);
        initialized = false;
    }

    private void drawButtonLabel(SimulationControlPanel.Button button) {
        int scale = button.label().length() > 7 ? 1 : 2;
        double textWidth = button.label().length() * 6d * scale - scale;
        double x = button.x() + Math.max(4d, (button.width() - textWidth) / 2d);
        double y = button.y() + (button.height() - 7d * scale) / 2d;
        drawText(button.label(), x, y, scale, 1f, 1f, 1f, 1f);
    }
}