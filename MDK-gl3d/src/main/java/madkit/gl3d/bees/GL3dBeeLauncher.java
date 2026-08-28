package madkit.gl3d.bees;

import madkit.simulation.EngineAgents;
import madkit.simulation.SimuLauncher;

/** MaDKit launcher for the CPU/OpenGL 3D bees scenario. */
@EngineAgents(scheduler = GL3dBeeScheduler.class, environment = GL3dBeeEnvironment.class,
        model = GL3dBeeModel.class, viewers = { GL3dBeeViewer.class })
public final class GL3dBeeLauncher extends SimuLauncher {
    public static void main(String[] args) {
        executeThisAgent(args);
    }
}
