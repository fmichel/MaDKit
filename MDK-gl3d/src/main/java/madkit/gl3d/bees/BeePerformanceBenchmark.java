package madkit.gl3d.bees;

import java.util.Arrays;
import java.util.Locale;

/**
 * Headless benchmark for the CPU simulation and immutable snapshot boundary.
 *
 * <p>This benchmark deliberately does not initialize GLFW or OpenGL. It reports
 * simulation and snapshot costs independently for the roadmap population tiers.
 * Allocation pressure and GPU frame time require a profiler or graphical run and
 * are therefore not inferred from these measurements.</p>
 */
public final class BeePerformanceBenchmark {
    /** Population tiers required by the MDK-gl3d roadmap. */
    public static final int[] DEFAULT_BEE_COUNTS = { 1_000, 10_000, 100_000 };
    private static final int DEFAULT_FLOWER_COUNT = 100;
    private static final int DEFAULT_WARMUP_TICKS = 5;
    private static final int DEFAULT_MEASURED_TICKS = 20;

    private BeePerformanceBenchmark() {
        // Utility class.
    }

    /**
     * Measures one deterministic population tier.
     *
     * @param beeCount number of worker bees
     * @param flowerCount number of flower patches
     * @param warmupTicks ticks discarded before measurement
     * @param measuredTicks ticks included in the result
     * @return measured tick and snapshot timings
     */
    public static BenchmarkResult measure(int beeCount, int flowerCount,
            int warmupTicks, int measuredTicks) {
        if (beeCount < 0 || flowerCount < 0 || warmupTicks < 0 || measuredTicks <= 0) {
            throw new IllegalArgumentException("benchmark sizes must be non-negative and measuredTicks must be positive");
        }
        BeeColonyModel model = new BeeColonyModel(0x4D444BL, beeCount, flowerCount);
        for (int i = 0; i < warmupTicks; i++) {
            model.advance();
            model.colonySnapshot();
        }

        long tickNanos = 0;
        long snapshotNanos = 0;
        long snapshotEntities = 0;
        for (int i = 0; i < measuredTicks; i++) {
            long start = System.nanoTime();
            model.advance();
            tickNanos += System.nanoTime() - start;

            start = System.nanoTime();
            ColonySnapshot snapshot = model.colonySnapshot();
            snapshotNanos += System.nanoTime() - start;
            snapshotEntities += snapshot.bees().size() + snapshot.flowers().size();
        }
        return new BenchmarkResult(beeCount, flowerCount, measuredTicks,
                tickNanos, snapshotNanos, snapshotEntities);
    }

    /** Runs all default population tiers and prints machine-readable CSV rows. */
    public static void main(String[] args) {
        int warmupTicks = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_WARMUP_TICKS;
        int measuredTicks = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_MEASURED_TICKS;
        System.out.println("beeCount,flowerCount,ticks,totalTickMs,avgTickUs,totalSnapshotMs,avgSnapshotUs");
        Arrays.stream(DEFAULT_BEE_COUNTS)
                .mapToObj(count -> measure(count, DEFAULT_FLOWER_COUNT, warmupTicks, measuredTicks))
                .forEach(result -> System.out.println(result.toCsv()));
    }

    /** Immutable result for one benchmark tier. */
    public record BenchmarkResult(int beeCount, int flowerCount, int measuredTicks,
            long tickNanos, long snapshotNanos, long snapshotEntities) {
        /** Average simulation tick duration in microseconds. */
        public double averageTickMicros() {
            return tickNanos / (double) measuredTicks / 1_000.0;
        }

        /** Average immutable snapshot creation duration in microseconds. */
        public double averageSnapshotMicros() {
            return snapshotNanos / (double) measuredTicks / 1_000.0;
        }

        /** Returns this result as a CSV data row. */
        public String toCsv() {
            return String.format(Locale.ROOT, "%d,%d,%d,%.3f,%.3f,%.3f,%.3f",
                    beeCount, flowerCount, measuredTicks, tickNanos / 1_000_000.0,
                    averageTickMicros(), snapshotNanos / 1_000_000.0, averageSnapshotMicros());
        }
    }
}