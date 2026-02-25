package madkit.grafana.metrics;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Immutable data point for simulation metrics.
 * <p>
 * Represents a single measurement with optional tags, a field name, a numeric value,
 * a nanosecond-precision timestamp, and optional extra fields.
 * Thread-safe by virtue of being an immutable record.
 *
 * @param measurement   the InfluxDB measurement name (e.g., "population")
 * @param tags          optional key-value tags for filtering (may be empty or null)
 * @param field         the field name (typically "value")
 * @param value         the numeric value
 * @param timestampNanos epoch timestamp in nanoseconds
 * @param extraFields   optional additional numeric fields (may be null or empty)
 */
public record DataPoint(
    String measurement,
    Map<String, String> tags,
    String field,
    double value,
    long timestampNanos,
    Map<String, Double> extraFields
) {
    /**
     * Convenience constructor with default field name "value" and auto-generated timestamp.
     *
     * @param measurement the measurement name
     * @param tags        optional tags
     * @param value       the numeric value
     */
    public DataPoint(String measurement, Map<String, String> tags, double value) {
        this(measurement, tags, "value", value, System.currentTimeMillis() * 1_000_000L, Map.of());
    }

    /**
     * Convenience constructor with extra fields, default field name "value",
     * and auto-generated timestamp.
     *
     * @param measurement the measurement name
     * @param tags        optional tags
     * @param value       the numeric value
     * @param extraFields additional numeric fields
     */
    public DataPoint(String measurement, Map<String, String> tags,
                     double value, Map<String, Double> extraFields) {
        this(measurement, tags, "value", value, System.currentTimeMillis() * 1_000_000L, extraFields);
    }

    /**
     * Converts this data point to InfluxDB line protocol format.
     *
     * @return a line-protocol string (e.g., "population,role=follower value=42.0 1740000000000000000")
     */
    public String toLineProtocol() {
        var sb = new StringBuilder(measurement);
        if (tags != null && !tags.isEmpty()) {
            sb.append(',');
            sb.append(tags.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(",")));
        }
        sb.append(' ').append(field).append('=').append(value);
        if (extraFields != null && !extraFields.isEmpty()) {
            extraFields.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> sb.append(',').append(e.getKey()).append('=').append(e.getValue()));
        }
        sb.append(' ').append(timestampNanos);
        return sb.toString();
    }
}
