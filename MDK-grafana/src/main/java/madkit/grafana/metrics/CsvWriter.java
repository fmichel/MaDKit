package madkit.grafana.metrics;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Writes simulation metrics to a CSV file.
 * <p>
 * Thread-safe: synchronized on the writer instance.
 * Creates the file with a header row on first use if the file does not already exist or is empty.
 */
public class CsvWriter implements MetricsWriter {

    private static final Logger LOGGER = Logger.getLogger(CsvWriter.class.getName());
    /** The CSV column header written as the first line of new files. */
    private static final String HEADER = "timestamp,measurement,tags,field,value,extraFields";

    /** The underlying writer for appending CSV lines. */
    private final PrintWriter writer;

    /**
     * Creates a CSV writer that appends to the given file.
     *
     * @param outputPath the path to the CSV file
     * @throws IOException if the file cannot be opened
     */
    public CsvWriter(Path outputPath) throws IOException {
        boolean fileExists = Files.exists(outputPath) && Files.size(outputPath) > 0;
        this.writer = new PrintWriter(Files.newBufferedWriter(outputPath,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND));
        if (!fileExists) {
            writer.println(HEADER);
        }
    }

    @Override
    public synchronized void writeBatch(List<DataPoint> points) {
        for (DataPoint dp : points) {
            writer.printf("%d,%s,%s,%s,%s,%s%n",
                    dp.timestampNanos(),
                    dp.measurement(),
                    formatTags(dp.tags()),
                    dp.field(),
                    dp.value(),
                    formatExtraFields(dp.extraFields()));
        }
    }

    @Override
    public synchronized void flush() {
        writer.flush();
    }

    @Override
    public synchronized void close() {
        writer.flush();
        writer.close();
    }

    /**
     * Formats a tag map as a semicolon-separated {@code key=value} string.
     *
     * @param tags the tag map (may be null or empty)
     * @return the formatted string, or empty string if no tags
     */
    private String formatTags(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) return "";
        return tags.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(";"));
    }

    /**
     * Formats an extra-fields map as a semicolon-separated {@code key=value} string.
     *
     * @param extraFields the extra-fields map (may be null or empty)
     * @return the formatted string, or empty string if no extra fields
     */
    private String formatExtraFields(Map<String, Double> extraFields) {
        if (extraFields == null || extraFields.isEmpty()) return "";
        return extraFields.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(";"));
    }
}
