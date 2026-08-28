package madkit.gl3d.bees;

/** Immutable render-independent view of one bee, including its last-tick velocity. */
public record BeeSnapshot(int id, Vec3 position, Vec3 velocity, BeeState state, double energy, double nectar) {
}