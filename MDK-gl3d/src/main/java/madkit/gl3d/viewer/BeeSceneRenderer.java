package madkit.gl3d.viewer;

import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_POINTS;
import static org.lwjgl.opengl.GL11C.GL_LINES;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL32C.GL_PROGRAM_POINT_SIZE;
import static org.lwjgl.opengl.GL11C.glBlendFunc;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL15C.glBufferSubData;
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
import static org.lwjgl.opengl.GL20C.glUniform3f;
import static org.lwjgl.opengl.GL20C.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL20C.glLinkProgram;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL31C.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33C.glVertexAttribDivisor;
import static org.lwjgl.opengl.GL33C.GL_TIME_ELAPSED;
import static org.lwjgl.opengl.GL15C.GL_QUERY_RESULT;
import static org.lwjgl.opengl.GL15C.GL_QUERY_RESULT_AVAILABLE;
import static org.lwjgl.opengl.GL15C.glBeginQuery;
import static org.lwjgl.opengl.GL15C.glDeleteQueries;
import static org.lwjgl.opengl.GL15C.glEndQuery;
import static org.lwjgl.opengl.GL15C.glGenQueries;
import static org.lwjgl.opengl.GL15C.glGetQueryObjecti;
import static org.lwjgl.opengl.GL33C.glGetQueryObjectui64;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import madkit.gl3d.bees.BeeSnapshot;
import madkit.gl3d.bees.BeeState;
import madkit.gl3d.bees.ColonySnapshot;
import madkit.gl3d.bees.FlowerSnapshot;

/**
 * GL-thread-owned renderer for a colony snapshot.
 *
 * <p>The context uses batched terrain lines and solid hive/flower geometry,
 * while bees use one static mesh with per-instance position and color data.
 * All OpenGL resource creation, updates, and cleanup must happen on the
 * thread owning the current context.</p>
 */
public final class BeeSceneRenderer implements AutoCloseable {
    private static final String VERTEX = "#version 330 core\n"
            + "layout (location = 0) in vec3 position;\n"
            + "uniform mat4 viewProjection;\n"
            + "void main() { gl_Position = viewProjection * vec4(position, 1.0); gl_PointSize = 7.0; }\n";
    private static final String FRAGMENT = "#version 330 core\n"
            + "uniform vec3 pointColor;\n"
            + "out vec4 color;\n"
            + "void main() { color = vec4(pointColor, 1.0); }\n";
    private static final String BEE_VERTEX = "#version 330 core\n"
            + "layout (location = 0) in vec3 vertexPosition;\n"
            + "layout (location = 1) in vec3 instancePosition;\n"
            + "layout (location = 2) in vec3 instanceColor;\n"
            + "uniform mat4 viewProjection;\n"
            + "out vec3 color;\n"
            + "void main() { gl_Position = viewProjection * vec4(instancePosition + vertexPosition, 1.0); color = instanceColor; }\n";
    private static final String BEE_FRAGMENT = "#version 330 core\n"
            + "in vec3 color;\n"
            + "out vec4 fragmentColor;\n"
            + "void main() { fragmentColor = vec4(color, 1.0); }\n";
    private static final float[] BEE_MESH = {
            -0.45f, 0f, 0f, 0.45f, 0f, 0f, 0f, 0.22f, 0.18f,
            0.45f, 0f, 0f, -0.45f, 0f, 0f, 0f, 0.22f, -0.18f,
            -0.45f, 0f, 0f, 0f, 0.22f, -0.18f, 0f, -0.18f, 0f,
            0.45f, 0f, 0f, 0f, -0.18f, 0f, 0f, 0.22f, 0.18f,
            -0.45f, 0f, 0f, 0f, -0.18f, 0f, 0f, 0.22f, -0.18f,
            0.45f, 0f, 0f, 0f, 0.22f, 0.18f, 0f, -0.18f, 0f
    };
    private static final int MAX_TRAIL_POINTS = 12;

    private int gpuTimerQuery;
    private boolean gpuTimerSupported;
    private double gpuFrameMicros;
    private int program;
    private int vao;
    private int positionBuffer;
    private int colorLocation;
    private int viewProjectionLocation;
    private boolean initialized;
    private final OrbitCamera camera = new OrbitCamera();
    private int beeProgram;
    private int beeVao;
    private int beeMeshBuffer;
    private int beeInstanceBuffer;
    private int beeViewProjectionLocation;
    private final Map<Integer, ArrayDeque<madkit.gl3d.bees.Vec3>> trailHistory = new HashMap<>();
    private long trailTick = -1;

    /** Renders the newest snapshot; must be called only from the GL context thread. */
    public void render(ColonySnapshot snapshot) {
        render(snapshot, 16f / 9f);
    }

    /** Renders a snapshot using the supplied framebuffer aspect ratio. */
    public void render(ColonySnapshot snapshot, float aspectRatio) {
        render(snapshot, aspectRatio, true);
    }

    /** Renders a snapshot with optional behavior-state colors for bees. */
    public void render(ColonySnapshot snapshot, float aspectRatio, boolean stateColors) {
        render(snapshot, aspectRatio, stateColors, false);
    }

    /** Renders a snapshot with optional state colors and velocity vectors. */
    public void render(ColonySnapshot snapshot, float aspectRatio, boolean stateColors,
            boolean debugVectors) {
        render(snapshot, aspectRatio, stateColors, debugVectors, false);
    }

    /** Renders a snapshot with optional state colors, vectors, and bounded trails. */
    public void render(ColonySnapshot snapshot, float aspectRatio, boolean stateColors,
            boolean debugVectors, boolean trails) {
        if (!Float.isFinite(aspectRatio) || aspectRatio <= 0) {
            throw new IllegalArgumentException("aspect ratio must be positive");
        }
        if (!initialized) initialize();
        if (gpuTimerSupported) {
            if (glGetQueryObjecti(gpuTimerQuery, GL_QUERY_RESULT_AVAILABLE) != 0) {
                gpuFrameMicros = glGetQueryObjectui64(gpuTimerQuery, GL_QUERY_RESULT) / 1_000.0;
            }
            glBeginQuery(GL_TIME_ELAPSED, gpuTimerQuery);
        }
        glClearColor(0.025f, 0.04f, 0.07f, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(org.lwjgl.opengl.GL11C.GL_SRC_ALPHA, org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_PROGRAM_POINT_SIZE);
        glUseProgram(program);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(viewProjectionLocation, false,
                    camera.projectionMatrix(aspectRatio).mul(camera.viewMatrix()).get(stack.mallocFloat(16)));
        }
        glBindVertexArray(vao);
        drawContext();
        drawBees(snapshot, stateColors, aspectRatio);
        if (debugVectors) drawVectors(snapshot, aspectRatio);
        if (trails) drawTrails(snapshot, aspectRatio);
        drawFlowers(snapshot);
        if (gpuTimerSupported) glEndQuery(GL_TIME_ELAPSED);
    }

    private void initialize() {
        program = createProgram();
        colorLocation = glGetUniformLocation(program, "pointColor");
        viewProjectionLocation = glGetUniformLocation(program, "viewProjection");
        vao = glGenVertexArrays();
        positionBuffer = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, positionBuffer);
        glBufferData(GL_ARRAY_BUFFER, 0, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        beeProgram = createProgram(BEE_VERTEX, BEE_FRAGMENT);
        beeViewProjectionLocation = glGetUniformLocation(beeProgram, "viewProjection");
        beeVao = glGenVertexArrays();
        beeMeshBuffer = glGenBuffers();
        beeInstanceBuffer = glGenBuffers();
        glBindVertexArray(beeVao);
        glBindBuffer(GL_ARRAY_BUFFER, beeMeshBuffer);
        FloatBuffer mesh = BufferUtils.createFloatBuffer(BEE_MESH.length).put(BEE_MESH).flip();
        glBufferData(GL_ARRAY_BUFFER, mesh, org.lwjgl.opengl.GL15C.GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, beeInstanceBuffer);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 24, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 24, 12);
        glEnableVertexAttribArray(2);
        glVertexAttribDivisor(1, 1);
        glVertexAttribDivisor(2, 1);
        gpuTimerSupported = org.lwjgl.opengl.GL.getCapabilities().OpenGL33;
        if (gpuTimerSupported) gpuTimerQuery = glGenQueries();
        initialized = true;
    }

    private void drawBees(ColonySnapshot snapshot, boolean stateColors, float aspectRatio) {
        if (snapshot.bees().isEmpty()) return;
        FloatBuffer instances = BufferUtils.createFloatBuffer(snapshot.bees().size() * 6);
        for (BeeSnapshot bee : snapshot.bees()) {
            instances.put((float) bee.position().x()).put((float) bee.position().y()).put((float) bee.position().z());
            float[] color = stateColors ? colorFor(bee.state()) : new float[] { 1.0f, 0.72f, 0.08f };
            instances.put(color);
        }
        instances.flip();
        glUseProgram(beeProgram);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(beeViewProjectionLocation, false,
                    camera.projectionMatrix(aspectRatio).mul(camera.viewMatrix()).get(stack.mallocFloat(16)));
        }
        glBindVertexArray(beeVao);
        glBindBuffer(GL_ARRAY_BUFFER, beeInstanceBuffer);
        glBufferData(GL_ARRAY_BUFFER, instances, GL_DYNAMIC_DRAW);
        glDrawArraysInstanced(GL_TRIANGLES, 0, BEE_MESH.length / 3, snapshot.bees().size());
    }

    private static float[] colorFor(BeeState state) {
        return switch (state) {
            case IN_HIVE -> new float[] { 0.95f, 0.75f, 0.15f };
            case SEARCHING -> new float[] { 0.25f, 0.75f, 1.0f };
            case COLLECTING -> new float[] { 0.35f, 1.0f, 0.35f };
            case RETURNING -> new float[] { 1.0f, 0.45f, 0.15f };
            case RESTING -> new float[] { 0.75f, 0.45f, 1.0f };
        };
    }

    private void drawFlowers(ColonySnapshot snapshot) {
        FloatBuffer positions = BufferUtils.createFloatBuffer(snapshot.flowers().size() * 27);
        for (FlowerSnapshot flower : snapshot.flowers()) {
            putFlower(positions, (float) flower.position().x(), (float) flower.position().y(),
                    (float) flower.position().z());
        }
        positions.flip();
        glUseProgram(program);
        glBindVertexArray(vao);
        glUniform3f(colorLocation, 0.35f, 0.95f, 0.45f);
        draw(positions, snapshot.flowers().size() * 9, GL_TRIANGLES);
    }

    /** Adds three crossed triangular petals around a flower position. */
    private static void putFlower(FloatBuffer target, float x, float y, float z) {
        float radius = 0.45f;
        target.put(x - radius).put(y).put(z).put(x + radius).put(y).put(z)
                .put(x).put(y + 0.8f).put(z);
        target.put(x).put(y).put(z - radius).put(x).put(y).put(z + radius)
                .put(x).put(y + 0.8f).put(z);
        target.put(x - radius).put(y).put(z).put(x).put(y).put(z - radius)
                .put(x).put(y + 0.55f).put(z);
    }

    private void drawContext() {
        FloatBuffer terrain = BufferUtils.createFloatBuffer(132);
        for (int coordinate = -30; coordinate <= 30; coordinate += 10) {
            terrain.put(-30).put(-1).put(coordinate).put(30).put(-1).put(coordinate);
            terrain.put(coordinate).put(-1).put(-30).put(coordinate).put(-1).put(30);
        }
        glUniform3f(colorLocation, 0.16f, 0.25f, 0.32f);
        int terrainVertices = terrain.position() / 3;
        draw(terrain.flip(), terrainVertices, GL_LINES);

        FloatBuffer hive = BufferUtils.createFloatBuffer(324);
        float min = -2.5f;
        float max = 2.5f;
        float base = -1f;
        float roof = 4f;
        putBoxFaces(hive, min, base, min, max, roof, max);
        glUniform3f(colorLocation, 0.9f, 0.55f, 0.12f);
        int hiveVertices = hive.position() / 3;
        draw(hive.flip(), hiveVertices, GL_TRIANGLES);
    }

    private static void putBoxFaces(FloatBuffer target, float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ) {
        float[][] corners = { { minX, minY, minZ }, { maxX, minY, minZ }, { maxX, minY, maxZ },
                { minX, minY, maxZ }, { minX, maxY, minZ }, { maxX, maxY, minZ },
                { maxX, maxY, maxZ }, { minX, maxY, maxZ } };
        int[][] faces = { { 0, 1, 2, 0, 2, 3 }, { 4, 6, 5, 4, 7, 6 },
                { 0, 4, 5, 0, 5, 1 }, { 1, 5, 6, 1, 6, 2 },
                { 2, 6, 7, 2, 7, 3 }, { 4, 0, 3, 4, 3, 7 } };
        for (int[] face : faces) {
            for (int corner : face) target.put(corners[corner]);
        }
    }

    /** Retained as a small geometry utility for wireframe/debug clients. */
    private static void putBoxEdges(FloatBuffer target, float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ) {
        float[][] corners = { { minX, minY, minZ }, { maxX, minY, minZ }, { maxX, minY, maxZ },
                { minX, minY, maxZ }, { minX, maxY, minZ }, { maxX, maxY, minZ },
                { maxX, maxY, maxZ }, { minX, maxY, maxZ } };
        int[][] edges = { { 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 0 }, { 4, 5 }, { 5, 6 },
                { 6, 7 }, { 7, 4 }, { 0, 4 }, { 1, 5 }, { 2, 6 }, { 3, 7 } };
        for (int[] edge : edges) {
            target.put(corners[edge[0]]).put(corners[edge[1]]);
        }
    }

    private void draw(FloatBuffer positions, int count, int primitive) {
        if (count == 0) return;
        glBindBuffer(GL_ARRAY_BUFFER, positionBuffer);
        glBufferData(GL_ARRAY_BUFFER, positions, GL_DYNAMIC_DRAW);
        glDrawArrays(primitive, 0, count);
    }

    private void draw(FloatBuffer positions, int count) {
        draw(positions, count, GL_POINTS);
    }

    private static int createProgram() {
        return createProgram(VERTEX, FRAGMENT);
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
        if (gpuTimerQuery != 0) {
            glDeleteQueries(gpuTimerQuery);
            gpuTimerQuery = 0;
        }
        glDeleteBuffers(positionBuffer);
        glDeleteVertexArrays(vao);
        glDeleteProgram(program);
        glDeleteBuffers(beeInstanceBuffer);
        glDeleteBuffers(beeMeshBuffer);
        glDeleteVertexArrays(beeVao);
        glDeleteProgram(beeProgram);
        trailHistory.clear();
        trailTick = -1;
        gpuTimerSupported = false;
        gpuFrameMicros = 0;
        initialized = false;
    }

    public OrbitCamera camera() {
        return camera;
    }

    /**
     * Returns the latest completed GPU scene time in microseconds.
     *
     * <p>The value is zero until an OpenGL 3.3 timer query completes or when timer queries are
     * unavailable. It excludes buffer swap and overlay rendering.</p>
     *
     * @return latest completed GPU scene time, or zero when unavailable
     */
    public double gpuFrameMicros() {
        return gpuFrameMicros;
    }

    private void drawVectors(ColonySnapshot snapshot, float aspectRatio) {
        if (snapshot.bees().isEmpty()) return;
        FloatBuffer vectors = BufferUtils.createFloatBuffer(snapshot.bees().size() * 6);
        for (BeeSnapshot bee : snapshot.bees()) {
            double x = bee.position().x();
            double y = bee.position().y();
            double z = bee.position().z();
            vectors.put((float) x).put((float) y).put((float) z);
            vectors.put((float) (x + bee.velocity().x() * 4.0))
                    .put((float) (y + bee.velocity().y() * 4.0))
                    .put((float) (z + bee.velocity().z() * 4.0));
        }
        glUseProgram(program);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(viewProjectionLocation, false,
                    camera.projectionMatrix(aspectRatio).mul(camera.viewMatrix()).get(stack.mallocFloat(16)));
        }
        glBindVertexArray(vao);
        glUniform3f(colorLocation, 1.0f, 0.9f, 0.2f);
        draw(vectors.flip(), snapshot.bees().size() * 2, GL_LINES);
    }

    private void drawTrails(ColonySnapshot snapshot, float aspectRatio) {
        updateTrailHistory(snapshot);
        int segmentCount = trailHistory.values().stream()
                .mapToInt(points -> Math.max(0, points.size() - 1)).sum();
        if (segmentCount == 0) return;
        FloatBuffer lines = BufferUtils.createFloatBuffer(segmentCount * 6);
        for (ArrayDeque<madkit.gl3d.bees.Vec3> points : trailHistory.values()) {
            madkit.gl3d.bees.Vec3 previous = null;
            for (madkit.gl3d.bees.Vec3 point : points) {
                if (previous != null) {
                    lines.put((float) previous.x()).put((float) previous.y()).put((float) previous.z());
                    lines.put((float) point.x()).put((float) point.y()).put((float) point.z());
                }
                previous = point;
            }
        }
        glUseProgram(program);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(viewProjectionLocation, false,
                    camera.projectionMatrix(aspectRatio).mul(camera.viewMatrix()).get(stack.mallocFloat(16)));
        }
        glBindVertexArray(vao);
        glUniform3f(colorLocation, 0.95f, 0.65f, 0.2f);
        draw(lines.flip(), segmentCount * 2, GL_LINES);
    }

    private void updateTrailHistory(ColonySnapshot snapshot) {
        if (snapshot.tick() < trailTick) trailHistory.clear();
        if (snapshot.tick() == trailTick) return;
        trailTick = snapshot.tick();
        java.util.HashSet<Integer> activeIds = new java.util.HashSet<>();
        for (BeeSnapshot bee : snapshot.bees()) {
            activeIds.add(bee.id());
            ArrayDeque<madkit.gl3d.bees.Vec3> points = trailHistory.computeIfAbsent(bee.id(), ignored -> new ArrayDeque<>());
            points.addLast(bee.position());
            while (points.size() > MAX_TRAIL_POINTS) points.removeFirst();
        }
        trailHistory.keySet().removeIf(id -> !activeIds.contains(id));
    }
}
