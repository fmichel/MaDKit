/**
 * Optional MaDKit 6 LWJGL/GLFW viewer extension and deterministic bees demo.
 * OpenGL resources are render-thread-owned; the bees model and controller are
 * usable headlessly for CI. CUDA support is optional and never required for
 * baseline startup.
 */
module madkit.gl3d {
    requires transitive madkit.base;
    requires org.lwjgl;
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;
    requires org.joml;

    exports madkit.gl3d;
    exports madkit.gl3d.viewer;
    exports madkit.gl3d.cuda;
    exports madkit.gl3d.bees;
    exports madkit.gl3d.gui;
    exports madkit.gl3d.demo;
}