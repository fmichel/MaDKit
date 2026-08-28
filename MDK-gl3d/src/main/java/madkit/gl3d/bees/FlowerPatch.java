package madkit.gl3d.bees;

/** Mutable simulation resource representing a flower field. */
public final class FlowerPatch {
    private final int id;
    private final Vec3 position;
    private final double capacity;
    private double nectar;

    public FlowerPatch(int id, Vec3 position, double nectar) {
        if (nectar < 0) throw new IllegalArgumentException("nectar must be non-negative");
        this.id = id;
        this.position = position;
        this.capacity = nectar;
        this.nectar = nectar;
    }

    public int id() { return id; }
    public Vec3 position() { return position; }
    public double nectar() { return nectar; }
    public double capacity() { return capacity; }

    public double collect(double amount) {
        double collected = Math.min(Math.max(amount, 0), nectar);
        nectar -= collected;
        return collected;
    }

    public void regenerate(double amount) {
        nectar = Math.min(capacity, nectar + Math.max(amount, 0));
    }
}
