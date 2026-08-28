package madkit.gl3d.bees;

/** Immutable render-facing view of one flower patch. */
public record FlowerSnapshot(int id, Vec3 position, double nectar, double capacity) {
}
