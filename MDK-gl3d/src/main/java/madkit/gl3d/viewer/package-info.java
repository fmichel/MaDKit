/**
 * Camera and render-thread-owned scene implementation.
 *
 * <p>{@link madkit.gl3d.viewer.BeeSceneRenderer} consumes immutable snapshots
 * and owns its VAOs, buffers, and shader programs. The baseline scene uses a
 * batched context and one instanced bee mesh draw. {@link
 * madkit.gl3d.viewer.OrbitCamera} is independent of GLFW/OpenGL and clamps
 * input before producing right-handed view/projection matrices.</p>
 */
package madkit.gl3d.viewer;
