/**
 * OpenGL/GLFW infrastructure for optional MaDKit 6 viewers.
 *
 * <p>The module keeps the MaDKit core API unchanged. GLFW and OpenGL resources
 * are owned by the render thread; scheduler threads may request display work
 * but must not call native graphics APIs directly.</p>
 */
package madkit.gl3d;
