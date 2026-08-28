package madkit.gl3d.bees;

/** Immutable, validated parameters used to create a deterministic bee scenario. */
public record ScenarioParameters(long seed, int beeCount, int flowerCount) {
    /** Lowest supported population change step. */
    public static final int COUNT_STEP = 10;
    /** Maximum population accepted by the interactive baseline. */
    public static final int MAX_BEE_COUNT = 100_000;
    /** Maximum flower-field size accepted by the interactive baseline. */
    public static final int MAX_FLOWER_COUNT = 10_000;

    public ScenarioParameters {
        if (beeCount < 0 || beeCount > MAX_BEE_COUNT) {
            throw new IllegalArgumentException("bee count must be in [0, 100000]");
        }
        if (flowerCount < 0 || flowerCount > MAX_FLOWER_COUNT) {
            throw new IllegalArgumentException("flower count must be in [0, 10000]");
        }
    }

    /** Returns the default deterministic demonstration scenario. */
    public static ScenarioParameters defaults() {
        return new ScenarioParameters(42, 250, 12);
    }

    /** Returns a copy with a different seed. */
    public ScenarioParameters withSeed(long newSeed) {
        return new ScenarioParameters(newSeed, beeCount, flowerCount);
    }

    /** Returns a copy with the bee count clamped to the supported range. */
    public ScenarioParameters withBeeCount(int newCount) {
        return new ScenarioParameters(seed, clamp(newCount, 0, MAX_BEE_COUNT), flowerCount);
    }

    /** Returns a copy with the flower count clamped to the supported range. */
    public ScenarioParameters withFlowerCount(int newCount) {
        return new ScenarioParameters(seed, beeCount, clamp(newCount, 0, MAX_FLOWER_COUNT));
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
