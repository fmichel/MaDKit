package madkit.gl3d;

/** Receives GLFW input events on the OpenGL thread. */
public interface GLInputHandler {
    default void key(int key, int action, int modifiers) { }
    default void cursorMoved(double x, double y) { }
    default void mouseButton(int button, int action, int modifiers) { }
    default void scroll(double xOffset, double yOffset) { }
}
