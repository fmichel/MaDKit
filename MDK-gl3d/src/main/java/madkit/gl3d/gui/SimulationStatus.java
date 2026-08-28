package madkit.gl3d.gui;

import java.util.Map;

import madkit.gl3d.bees.BeeState;
import madkit.gl3d.bees.SimulationController;

/** Immutable status data consumed by a simulation control panel or overlay. */
public record SimulationStatus(SimulationController.State state, long tick, int beeCount,
                               double foodStore, double simulationSpeed, double renderFps,
                               Map<BeeState, Integer> stateCounts, double simulatedTimeSeconds,
                               double simulationTickMicros, double renderFrameMicros, double gpuFrameMicros) {
    public SimulationStatus {
        stateCounts = Map.copyOf(stateCounts);
    }
}