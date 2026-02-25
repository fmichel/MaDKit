package madkit.grafana.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Central metrics facade used by MaDKit agents to record simulation data.
 * <p>
 * Thread-safe: multiple agents may call {@code record()} concurrently.
 * Data points are buffered and flushed to all registered {@link MetricsWriter}
 * instances by a daemon background thread.
 *
 * <h2>Usage example</h2>
 * {@code
 * var writer = new CsvWriter(Path.of("metrics.csv"));
 * var metrics = new SimuMetrics(writer);
 * metrics.record("population", 42.0);
 * metrics.record("population", 5.0, Map.of("role", "queen"));
 * metrics.close();
 * }
 */
public class SimuMetrics implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(SimuMetrics.class.getName());
    /** Default capacity of the data-point buffer. */
    static final int DEFAULT_BUFFER_SIZE = 10_000;
    /** Default interval in milliseconds between background flushes. */
    static final long DEFAULT_FLUSH_INTERVAL_MS = 500;
    /** Maximum number of data points drained per flush cycle. */
    static final int BATCH_THRESHOLD = 100;

    /** Bounded buffer holding data points between record() calls and flush cycles. */
    private final BlockingQueue<DataPoint> buffer;
    /** Writers that receive flushed batches (InfluxDB, CSV, etc.). */
    private final List<MetricsWriter> writers;
    /** Single-thread scheduled executor that triggers periodic flushes. */
    private final ScheduledExecutorService flushExecutor;
    /** Guard flag: once true, all record() calls are silently discarded. */
    private volatile boolean closed = false;

    /**
     * Creates a SimuMetrics instance with a custom flush interval.
     *
     * @param flushIntervalMs interval in milliseconds between background flushes
     * @param writers         one or more MetricsWriter implementations
     */
    public SimuMetrics(long flushIntervalMs, MetricsWriter... writers) {
        this.buffer = new ArrayBlockingQueue<>(DEFAULT_BUFFER_SIZE);
        this.writers = new CopyOnWriteArrayList<>(List.of(writers));
        this.flushExecutor = createFlushExecutor();
        this.flushExecutor.scheduleAtFixedRate(
                this::flushBuffer, flushIntervalMs, flushIntervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a SimuMetrics instance with the given writers.
     *
     * @param writers one or more MetricsWriter implementations
     */
    public SimuMetrics(MetricsWriter... writers) {
        this(DEFAULT_FLUSH_INTERVAL_MS, writers);
    }

    /** Protected no-arg constructor for the {@link NoOpSimuMetrics} subclass. */
    protected SimuMetrics() {
        this.buffer = null;
        this.writers = List.of();
        this.flushExecutor = null;
    }

    /**
     * Records a gauge value with no tags.
     *
     * @param measurement the measurement name (e.g., "population")
     * @param value       the numeric value
     */
    public void record(String measurement, double value) {
        record(measurement, value, Map.of());
    }

    /**
     * Records a gauge value with tags for Grafana filtering.
     *
     * @param measurement the measurement name
     * @param value       the numeric value
     * @param tags        key-value tags for InfluxDB filtering (e.g., {@code Map.of("role", "queen")})
     */
    public void record(String measurement, double value, Map<String, String> tags) {
        if (closed) return;
        var dp = new DataPoint(measurement, tags, "value", value, epochNanos(), Map.of());
        if (!buffer.offer(dp)) {
            LOGGER.warning(() -> "Metrics buffer full — dropping data point: " + measurement);
        }
    }

    /**
     * Records a gauge value with tags and an explicit nanosecond timestamp.
     *
     * @param measurement    the measurement name
     * @param value          the numeric value
     * @param tags           tags for Grafana filtering
     * @param timestampNanos epoch timestamp in nanoseconds
     */
    public void record(String measurement, double value, Map<String, String> tags, long timestampNanos) {
        if (closed) return;
        var dp = new DataPoint(measurement, tags, "value", value, timestampNanos, Map.of());
        if (!buffer.offer(dp)) {
            LOGGER.warning(() -> "Metrics buffer full — dropping data point: " + measurement);
        }
    }

    /**
     * Records a gauge value with an explicit nanosecond timestamp and no tags.
     *
     * @param measurement    the measurement name
     * @param value          the numeric value
     * @param timestampNanos epoch timestamp in nanoseconds
     */
    public void record(String measurement, double value, long timestampNanos) {
        record(measurement, value, Map.of(), timestampNanos);
    }

    /**
     * Records a gauge value with an associated simulation tick.
     * The tick is stored as an extra InfluxDB field named "tick".
     *
     * @param measurement the measurement name
     * @param value       the numeric value
     * @param tick        the simulation tick (e.g., from TickBasedTimer.getCurrentTime())
     */
    public void record(String measurement, double value, double tick) {
        record(measurement, value, Map.of(), tick);
    }

    /**
     * Records a gauge value with tags and an associated simulation tick.
     *
     * @param measurement the measurement name
     * @param value       the numeric value
     * @param tags        tags for Grafana filtering
     * @param tick        the simulation tick
     */
    public void record(String measurement, double value, Map<String, String> tags, double tick) {
        if (closed) return;
        var dp = new DataPoint(measurement, tags, "value", value, epochNanos(), Map.of("tick", tick));
        if (!buffer.offer(dp)) {
            LOGGER.warning(() -> "Metrics buffer full — dropping data point: " + measurement);
        }
    }

    /**
     * Records a counter increment (value = 1.0).
     *
     * @param measurement the measurement name to increment
     */
    public void increment(String measurement) {
        record(measurement, 1.0);
    }

    @Override
    public void close() {
        closed = true;
        if (flushExecutor != null) {
            flushExecutor.shutdown();
        }
        drainRemainingPoints();
        closeAllWriters();
    }

    /**
     * Returns a no-op instance for when metrics are disabled.
     *
     * @return a singleton {@link NoOpSimuMetrics}
     */
    public static SimuMetrics noOp() {
        return NoOpSimuMetrics.INSTANCE;
    }

    /**
     * Drains up to {@link #BATCH_THRESHOLD} points from the buffer and delegates to writers.
     */
    private void flushBuffer() {
        List<DataPoint> batch = new ArrayList<>(BATCH_THRESHOLD);
        buffer.drainTo(batch, BATCH_THRESHOLD);
        if (!batch.isEmpty()) {
            delegateToWriters(batch);
        }
    }

    /**
     * Called on close: drains all remaining points from the buffer and flushes writers.
     */
    private void drainRemainingPoints() {
        if (buffer == null) return;
        List<DataPoint> remaining = new ArrayList<>();
        buffer.drainTo(remaining);
        if (!remaining.isEmpty()) {
            delegateToWriters(remaining);
            writers.forEach(MetricsWriter::flush);
        }
    }

    /**
     * Sends a batch of data points to every registered writer.
     *
     * @param batch the list of data points to write
     */
    private void delegateToWriters(List<DataPoint> batch) {
        for (MetricsWriter writer : writers) {
            writer.writeBatch(batch);
        }
    }

    /**
     * Closes every registered writer, logging any errors that occur.
     */
    private void closeAllWriters() {
        for (MetricsWriter writer : writers) {
            try {
                writer.close();
            } catch (Exception e) {
                LOGGER.warning(() -> "Error closing writer: " + e.getMessage());
            }
        }
    }

    /**
     * Creates a daemon {@link ScheduledExecutorService} for background flushing.
     *
     * @return a single-thread scheduled executor with a daemon thread
     */
    private ScheduledExecutorService createFlushExecutor() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SimuMetrics-flush");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Returns the current wall-clock time in nanoseconds (millisecond precision).
     *
     * @return epoch nanoseconds
     */
    private static long epochNanos() {
        return System.currentTimeMillis() * 1_000_000L;
    }
}
