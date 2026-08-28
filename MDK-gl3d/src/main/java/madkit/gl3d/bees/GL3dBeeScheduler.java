package madkit.gl3d.bees;

import madkit.simulation.scheduler.TickBasedScheduler;

/** Advances the GL3D bees model once for each MaDKit simulation tick. */
public final class GL3dBeeScheduler extends TickBasedScheduler {
    @Override
    public void doSimulationStep() {
        GL3dBeeModel model = getLauncher().getModel();
        model.advanceSimulation();
        super.doSimulationStep();
    }
}