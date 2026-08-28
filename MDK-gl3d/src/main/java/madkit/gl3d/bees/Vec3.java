package madkit.gl3d.bees;

/** Immutable three-dimensional coordinate or vector. */
public record Vec3(double x, double y, double z) {
    public double distanceSquared(Vec3 other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public Vec3 moveToward(Vec3 target, double distance) {
        double dx = target.x - x;
        double dy = target.y - y;
        double dz = target.z - z;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length == 0 || distance >= length) return target;
        double scale = distance / length;
        return new Vec3(x + dx * scale, y + dy * scale, z + dz * scale);
    }
}
