package madkit.gl3d.bees;

import java.util.List;

/** Immutable, complete state published from the simulation to the renderer. */
public record ColonySnapshot(long tick, List<BeeSnapshot> bees, List<FlowerSnapshot> flowers, double foodStore) {
    public ColonySnapshot {
        bees = List.copyOf(bees);
        flowers = List.copyOf(flowers);
    }
}
