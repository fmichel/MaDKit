package madkit.grafana.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.testng.annotations.Test;

public class DataPointTest {

    @Test
    public void givenMeasurementAndTags_whenToLineProtocol_thenCorrectFormat() {
        // Given
        var dp = new DataPoint("population", Map.of("role", "follower"), "value", 42.0, 1740000000000000000L, Map.of());

        // When
        String line = dp.toLineProtocol();

        // Then
        assertThat(line)
                .as("line protocol output")
                .isEqualTo("population,role=follower value=42.0 1740000000000000000");
    }

    @Test
    public void givenNoTags_whenToLineProtocol_thenNoTagSection() {
        // Given
        var dp = new DataPoint("cpu", Map.of(), "value", 99.5, 1000L, Map.of());

        // When
        String line = dp.toLineProtocol();

        // Then
        assertThat(line)
                .as("line protocol without tags")
                .isEqualTo("cpu value=99.5 1000");
    }

    @Test
    public void givenNullTags_whenToLineProtocol_thenNoTagSection() {
        // Given
        var dp = new DataPoint("cpu", null, "value", 99.5, 1000L, Map.of());

        // When
        String line = dp.toLineProtocol();

        // Then
        assertThat(line)
                .as("line protocol with null tags")
                .isEqualTo("cpu value=99.5 1000");
    }

    @Test
    public void givenMultipleTags_whenToLineProtocol_thenAllTagsPresent() {
        // Given
        var dp = new DataPoint("mem", Map.of("host", "srv1", "region", "eu"), "used", 8192.0, 2000L, Map.of());

        // When
        String line = dp.toLineProtocol();

        // Then
        assertThat(line)
                .as("line protocol with multiple tags")
                .contains("host=srv1")
                .contains("region=eu")
                .startsWith("mem,")
                .contains("used=8192.0 2000");
    }

    @Test
    public void givenConvenienceConstructor_whenCreated_thenFieldIsValueAndTimestampIsPositive() {
        // Given / When
        var dp = new DataPoint("test", Map.of(), 1.0);

        // Then
        assertThat(dp.field()).as("default field name").isEqualTo("value");
        assertThat(dp.timestampNanos()).as("auto-generated timestamp").isGreaterThan(0);
    }

    @Test
    public void givenExtraFields_whenToLineProtocol_thenAllFieldsPresent() {
        // Given
        var dp = new DataPoint("population", Map.of("role", "follower"), "value", 42.0,
                1740000000000000000L, Map.of("tick", 5.0));

        // When
        String line = dp.toLineProtocol();

        // Then
        assertThat(line)
                .as("line protocol with extra fields")
                .isEqualTo("population,role=follower value=42.0,tick=5.0 1740000000000000000");
    }

    @Test
    public void givenEmptyExtraFields_whenToLineProtocol_thenOnlyPrimaryField() {
        // Given
        var dp = new DataPoint("cpu", Map.of(), "value", 99.5, 1000L, Map.of());

        // When
        String line = dp.toLineProtocol();

        // Then
        assertThat(line)
                .as("line protocol with empty extra fields")
                .isEqualTo("cpu value=99.5 1000");
    }

    @Test
    public void givenNullExtraFields_whenToLineProtocol_thenOnlyPrimaryField() {
        // Given
        var dp = new DataPoint("cpu", Map.of(), "value", 99.5, 1000L, null);

        // When
        String line = dp.toLineProtocol();

        // Then
        assertThat(line)
                .as("line protocol with null extra fields")
                .isEqualTo("cpu value=99.5 1000");
    }

    @Test
    public void givenExtraFields_whenConvenienceConstructor_thenFieldIsValueAndTimestampPositive() {
        // Given / When
        var dp = new DataPoint("test", Map.of("env", "prod"), 7.0, Map.of("tick", 17.0, "step", 3.0));

        // Then
        assertThat(dp.field()).as("default field name").isEqualTo("value");
        assertThat(dp.timestampNanos()).as("auto-generated timestamp").isGreaterThan(0);
        assertThat(dp.extraFields())
                .as("extra fields content")
                .containsEntry("tick", 17.0)
                .containsEntry("step", 3.0);
    }
}
