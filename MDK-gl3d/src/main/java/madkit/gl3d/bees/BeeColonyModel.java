package madkit.gl3d.bees;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

/** Deterministic, headless bee-foraging model used by the 3D demonstrator. */
public final class BeeColonyModel {
    private static final double HIVE_RADIUS_SQUARED = 4.0;
    private static final double FLOWER_RADIUS_SQUARED = 9.0;
    private static final double BEE_SPEED = 1.5;

    private final long seed;
    private final Vec3 hivePosition;
    private final List<Bee> bees;
    private final List<FlowerPatch> flowers;
    private long tick;
    private double foodStore;

    public BeeColonyModel(long seed, int beeCount, int flowerCount) {
        if (beeCount < 0 || flowerCount < 0) throw new IllegalArgumentException("counts must be non-negative");
        this.seed = seed;
        this.hivePosition = new Vec3(0, 0, 0);
        this.bees = new ArrayList<>(beeCount);
        this.flowers = new ArrayList<>(flowerCount);
        SplittableRandom random = new SplittableRandom(seed);
        for (int i = 0; i < flowerCount; i++) {
            flowers.add(new FlowerPatch(i, new Vec3(random.nextDouble(-30, 30), random.nextDouble(0, 8), random.nextDouble(-30, 30)), 10));
        }
        for (int i = 0; i < beeCount; i++) {
            bees.add(new Bee(i, hivePosition));
        }
    }

    public long seed() { return seed; }
    public long tick() { return tick; }
    public double foodStore() { return foodStore; }
    public int beeCount() { return bees.size(); }
    public int flowerCount() { return flowers.size(); }
    public List<FlowerPatch> flowers() { return List.copyOf(flowers); }

    public void advance() {
        for (Bee bee : bees) {
            bee.advance(this);
        }
        for (FlowerPatch flower : flowers) flower.regenerate(0.01);
        tick++;
    }

    void deposit(double amount) { foodStore += amount; }
    Vec3 hivePosition() { return hivePosition; }
    FlowerPatch flowerFor(int beeId) { return flowers.get(beeId % flowers.size()); }

    public List<BeeSnapshot> snapshot() {
        return bees.stream().map(Bee::snapshot).toList();
    }

    public ColonySnapshot colonySnapshot() {
        return new ColonySnapshot(tick, snapshot(), flowers.stream()
                .map(flower -> new FlowerSnapshot(flower.id(), flower.position(), flower.nectar(), flower.capacity()))
                .toList(), foodStore);
    }

    /** Returns a stable count for every behavior state at the current model tick. */
    public Map<BeeState, Integer> stateCounts() {
        EnumMap<BeeState, Integer> counts = new EnumMap<>(BeeState.class);
        for (BeeState state : BeeState.values()) counts.put(state, 0);
        for (Bee bee : bees) counts.compute(bee.state, (state, count) -> count + 1);
        return Map.copyOf(counts);
    }

    private static final class Bee {
        private final int id;
        private Vec3 position;
        private Vec3 velocity = new Vec3(0, 0, 0);
        private BeeState state = BeeState.IN_HIVE;
        private double energy = 100;
        private double nectar;

        Bee(int id, Vec3 position) { this.id = id; this.position = position; }

        void advance(BeeColonyModel model) {
            Vec3 previousPosition = position;
            if (model.flowers.isEmpty()) {
                velocity = new Vec3(0, 0, 0);
                return;
            }
            FlowerPatch flower = model.flowerFor(id);
            switch (state) {
                case IN_HIVE, RESTING -> { state = BeeState.SEARCHING; position = position.moveToward(flower.position(), BEE_SPEED); }
                case SEARCHING -> { position = position.moveToward(flower.position(), BEE_SPEED); if (position.distanceSquared(flower.position()) <= FLOWER_RADIUS_SQUARED) state = BeeState.COLLECTING; }
                case COLLECTING -> { nectar += flower.collect(1); energy = Math.max(0, energy - 0.5); state = nectar > 0 ? BeeState.RETURNING : BeeState.SEARCHING; }
                case RETURNING -> { position = position.moveToward(model.hivePosition, BEE_SPEED); if (position.distanceSquared(model.hivePosition) <= HIVE_RADIUS_SQUARED) { model.deposit(nectar); nectar = 0; state = energy < 30 ? BeeState.RESTING : BeeState.SEARCHING; } }
            }
            energy = Math.max(0, energy - 0.05);
            velocity = new Vec3(position.x() - previousPosition.x(), position.y() - previousPosition.y(),
                    position.z() - previousPosition.z());
        }

        BeeSnapshot snapshot() { return new BeeSnapshot(id, position, velocity, state, energy, nectar); }
    }
}