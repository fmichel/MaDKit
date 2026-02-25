package madkit.grafana.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class PanelDefinitionTest {

    @Test
    public void givenPanelDefinition_whenCreated_thenFieldsAreAccessible() {
        // Given / When
        var panel = new PanelDefinition("CPU Usage", "timeseries", "cpu", "value");

        // Then
        assertThat(panel.title()).isEqualTo("CPU Usage");
        assertThat(panel.panelType()).isEqualTo("timeseries");
        assertThat(panel.measurement()).isEqualTo("cpu");
        assertThat(panel.field()).isEqualTo("value");
    }

    @Test
    public void givenTwoEqualPanels_whenCompared_thenEqual() {
        // Given
        var panel1 = new PanelDefinition("A", "stat", "m", "f");
        var panel2 = new PanelDefinition("A", "stat", "m", "f");

        // When / Then
        assertThat(panel1).as("record equality").isEqualTo(panel2);
    }
}
