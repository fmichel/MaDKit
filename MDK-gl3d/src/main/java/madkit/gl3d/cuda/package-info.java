/**
 * Optional CUDA capability and interop contracts.
 *
 * <p>CUDA is not a baseline dependency. Capability detection is non-initializing
 * and callers must fall back to CPU/OpenGL when JCuda or a compatible device is
 * unavailable. Any future CUDA/OpenGL mapping must be performed on the thread
 * that owns the current OpenGL context and must complete map, kernel, and
 * unmap operations before the buffer is drawn.</p>
 */
package madkit.gl3d.cuda;
