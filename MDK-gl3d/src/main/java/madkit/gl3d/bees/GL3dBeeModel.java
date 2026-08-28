package madkit.gl3d.bees;

import madkit.simulation.SimuModel;

/** MaDKit model-agent adapter around the deterministic GL3D bees model. */
public final class GL3dBeeModel extends SimuModel {
    private static final long DEFAULT_SEED = 42;
    private static final int DEFAULT_BEE_COUNT = 250;
    private static final int DEFAULT_FLOWER_COUNT = 12;
    private static final int MAX_BEE_COUNT = 100_000;
    private static final int MAX_FLOWER_COUNT = 10_000;

    private BeeColonyModel colony;
    private final ColonySnapshotExchange snapshots = new ColonySnapshotExchange();
    private SimulationController controller;

    public GL3dBeeModel() {
        configure(DEFAULT_SEED, DEFAULT_BEE_COUNT, DEFAULT_FLOWER_COUNT);
        snapshots.publish(colony.colonySnapshot());
    }

    @Override
    protected void onActivation() {
        super.onActivation();
        var configuration = getLauncher().getKernelConfig();
        configure(propertyLong("beeSeed", configuration.getLong("beeSeed", DEFAULT_SEED)),
                bounded(propertyInt("beeCount", configuration.getInt("beeCount", DEFAULT_BEE_COUNT)), 0, MAX_BEE_COUNT),
                bounded(propertyInt("flowerCount", configuration.getInt("flowerCount", DEFAULT_FLOWER_COUNT)), 0,
                        MAX_FLOWER_COUNT));
        snapshots.publish(colony.colonySnapshot());
    }

    private static int propertyInt(String name, int fallback) {
        try {
            return Integer.parseInt(System.getProperty(name, Integer.toString(fallback)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static long propertyLong(String name, long fallback) {
        try {
            return Long.parseLong(System.getProperty(name, Long.toString(fallback)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private synchronized void configure(long seed, int beeCount, int flowerCount) {
        colony = new BeeColonyModel(seed, beeCount, flowerCount);
        controller = new SimulationController(colony);
    }

    private static int bounded(int value, int minimum, int maximum) {
        return Math.clamp(value, minimum, maximum);
    }

    public synchronized void advanceSimulation() {
        controller.update();
        snapshots.publish(controller.model().colonySnapshot());
    }

    public synchronized ColonySnapshot latestSnapshot() {
        ColonySnapshot latest = snapshots.latestOrEmpty();
        ColonySnapshot current = controller.model().colonySnapshot();
        if (latest.tick() != current.tick() || latest.bees().size() != current.bees().size()
                || latest.flowers().size() != current.flowers().size()) {
            snapshots.publish(current);
            return current;
        }
        return latest;
    }

    public synchronized BeeColonyModel colony() {
        return controller.model();
    }

    public SimulationController controller() {
        return controller;
    }
}