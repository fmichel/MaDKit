package madkit.grafana.metrics;

import java.util.List;

/**
 * Strategy interface for writing simulation metrics to a backend.
 * <p>
 * Implementations must be thread-safe: {@link SimuMetrics} may call
 * {@link #writeBatch(List)} from a background flush thread.
 */
public interface MetricsWriter extends AutoCloseable {

    /**
     * Writes a batch of data points to the backend.
     *
     * @param points the data points to write (never null, may be empty)
     */
    void writeBatch(List<DataPoint> points);

    /**
     * Flushes any buffered data to the backend.
     */
    void flush();

    /**
     * Closes this writer and releases resources.
     */
    @Override
    void close();
}
