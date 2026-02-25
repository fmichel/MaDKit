package madkit.grafana.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

public class CsvWriterTest {

    @Test
    public void givenNewFile_whenWriteBatch_thenHeaderAndDataWritten() throws Exception {
        // Given
        Path tempFile = Files.createTempFile("csv-test-", ".csv");
        tempFile.toFile().deleteOnExit();
        var writer = new CsvWriter(tempFile);
        var points = List.of(
            new DataPoint("pop", Map.of("role", "bee"), "value", 100.0, 1000L, Map.of()),
            new DataPoint("pop", Map.of("role", "queen"), "value", 3.0, 2000L, Map.of())
        );

        // When
        writer.writeBatch(points);
        writer.close();

        // Then
        List<String> lines = Files.readAllLines(tempFile);
        assertThat(lines).as("CSV lines").hasSize(3); // header + 2 data
        assertThat(lines.get(0)).as("CSV header").isEqualTo("timestamp,measurement,tags,field,value,extraFields");
        assertThat(lines.get(1)).as("first data line").contains("pop").contains("100.0");
    }

    @Test
    public void givenExistingFile_whenWriteBatch_thenNoSecondHeader() throws Exception {
        // Given
        Path tempFile = Files.createTempFile("csv-append-", ".csv");
        tempFile.toFile().deleteOnExit();
        var writer1 = new CsvWriter(tempFile);
        writer1.writeBatch(List.of(new DataPoint("m", Map.of(), "v", 1.0, 1L, Map.of())));
        writer1.close();

        // When — open same file again
        var writer2 = new CsvWriter(tempFile);
        writer2.writeBatch(List.of(new DataPoint("m", Map.of(), "v", 2.0, 2L, Map.of())));
        writer2.close();

        // Then
        List<String> lines = Files.readAllLines(tempFile);
        long headerCount = lines.stream().filter(l -> l.equals("timestamp,measurement,tags,field,value,extraFields")).count();
        assertThat(headerCount).as("header should appear only once").isEqualTo(1);
        assertThat(lines).as("total lines (1 header + 2 data)").hasSize(3);
    }

    @Test
    public void givenEmptyBatch_whenWriteBatch_thenNoDataWritten() throws Exception {
        // Given
        Path tempFile = Files.createTempFile("csv-empty-", ".csv");
        tempFile.toFile().deleteOnExit();
        var writer = new CsvWriter(tempFile);

        // When
        writer.writeBatch(List.of());
        writer.close();

        // Then
        List<String> lines = Files.readAllLines(tempFile);
        assertThat(lines).as("only header, no data").hasSize(1);
    }

    @Test
    public void givenExtraFields_whenWriteBatch_thenExtraFieldsColumnPresent() throws Exception {
        // Given
        Path tempFile = Files.createTempFile("csv-extra-", ".csv");
        tempFile.toFile().deleteOnExit();
        var writer = new CsvWriter(tempFile);
        var points = List.of(
            new DataPoint("pop", Map.of("role", "bee"), "value", 100.0, 1000L, Map.of("tick", 5.0))
        );

        // When
        writer.writeBatch(points);
        writer.close();

        // Then
        List<String> lines = Files.readAllLines(tempFile);
        assertThat(lines).as("CSV lines").hasSize(2); // header + 1 data
        assertThat(lines.get(0)).as("CSV header").contains("extraFields");
        assertThat(lines.get(1)).as("data line should contain tick extra field").contains("tick=5.0");
    }

    @Test
    public void givenNoExtraFields_whenWriteBatch_thenExtraFieldsColumnEmpty() throws Exception {
        // Given
        Path tempFile = Files.createTempFile("csv-noextra-", ".csv");
        tempFile.toFile().deleteOnExit();
        var writer = new CsvWriter(tempFile);
        var points = List.of(
            new DataPoint("pop", Map.of(), "value", 42.0, 1000L, Map.of())
        );

        // When
        writer.writeBatch(points);
        writer.close();

        // Then
        List<String> lines = Files.readAllLines(tempFile);
        assertThat(lines).as("CSV lines").hasSize(2);
        // The line should end with an empty extraFields column (trailing comma or empty field)
        String dataLine = lines.get(1);
        // Count commas to verify 6 columns (5 commas)
        long commaCount = dataLine.chars().filter(c -> c == ',').count();
        assertThat(commaCount).as("should have 6 CSV columns (5 commas)").isEqualTo(5);
    }
}
