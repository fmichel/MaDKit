package madkit.gl3d.viewer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Clamped orbit camera for the 3D scene; independent of GLFW and OpenGL. */
public final class OrbitCamera {
    private static final float MIN_DISTANCE = 1f;
    private static final float MAX_DISTANCE = 1_000f;
    private static final float MIN_ELEVATION = (float) (-Math.PI / 2 + 0.01);
    private static final float MAX_ELEVATION = (float) (Math.PI / 2 - 0.01);

    private final Vector3f target = new Vector3f();
    private float azimuth = 0.7f;
    private float elevation = 0.55f;
    private float distance = 65f;
    private float fieldOfView = 60f;

    public void reset() {
        target.set(0, 0, 0);
        azimuth = 0.7f;
        elevation = 0.55f;
        distance = 65f;
        fieldOfView = 60f;
    }

    public void orbit(float azimuthDelta, float elevationDelta) {
        azimuth += azimuthDelta;
        elevation = clamp(elevation + elevationDelta, MIN_ELEVATION, MAX_ELEVATION);
    }

    public void pan(float horizontal, float vertical) {
        target.x += horizontal * distance * 0.002f;
        target.z += vertical * distance * 0.002f;
    }

    public void zoom(float amount) {
        distance = clamp(distance * (float) Math.exp(amount * 0.1), MIN_DISTANCE, MAX_DISTANCE);
    }

    public void focus(Vector3f point) {
        target.set(point);
    }

    public Vector3f position() {
        float horizontal = distance * (float) Math.cos(elevation);
        return new Vector3f(target.x + horizontal * (float) Math.sin(azimuth),
                target.y + distance * (float) Math.sin(elevation),
                target.z + horizontal * (float) Math.cos(azimuth));
    }

    public Matrix4f viewMatrix() {
        return new Matrix4f().lookAt(position(), target, new Vector3f(0, 1, 0));
    }

    public Matrix4f projectionMatrix(float aspectRatio) {
        if (!Float.isFinite(aspectRatio) || aspectRatio <= 0) throw new IllegalArgumentException("aspect ratio must be positive");
        return new Matrix4f().perspective((float) Math.toRadians(fieldOfView), aspectRatio, 0.1f, MAX_DISTANCE * 2);
    }

    public float distance() { return distance; }
    public float fieldOfView() { return fieldOfView; }
    public void fieldOfView(float value) { fieldOfView = clamp(value, 10, 120); }
    public Vector3f target() { return new Vector3f(target); }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
