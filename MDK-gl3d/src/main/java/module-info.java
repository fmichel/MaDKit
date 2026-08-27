module madkit.gl3d {
    requires transitive madkit.base;
    requires org.lwjgl;
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;
    requires org.joml;

    exports madkit.gl3d;
    exports madkit.gl3d.viewer;
    exports madkit.gl3d.cuda;
    exports madkit.gl3d.demo;
}
