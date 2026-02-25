package madkit.grafana.metrics;

import java.util.Map;

/**
 * A no-op implementation of {@link SimuMetrics} that silently discards all data.
 * <p>
 * Used when the Grafana infrastructure is unavailable or metrics are explicitly disabled.
 * All methods are zero-cost no-ops: no buffer, no flush thread, no I/O.
 */
final class NoOpSimuMetrics extends SimuMetrics {

    /** Singleton instance returned by {@link SimuMetrics#noOp()}. */
    static final NoOpSimuMetrics INSTANCE = new NoOpSimuMetrics();

    /**
     * Private constructor — prevents instantiation outside this class.
     * Does not call super() with writers to avoid buffer and flush thread creation.
     */
    private NoOpSimuMetrics() {
        // Do NOT call super() with writers — skip buffer and flush thread creation entirely.
    }

    @Override
    public void record(String measurement, double value) {
        // no-op
    }

    @Override
    public void record(String measurement, double value, Map<String, String> tags) {
        // no-op
    }

    @Override
    public void record(String measurement, double value, Map<String, String> tags, long timestampNanos) {
        // no-op
    }

    @Override
    public void record(String measurement, double value, long timestampNanos) {
        // no-op
    }

    @Override
    public void increment(String measurement) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }
}
