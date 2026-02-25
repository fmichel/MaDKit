package madkit.grafana.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import org.testng.annotations.Test;

public class XYPanelDefinitionTest {

    @Test
    public void givenXYPanelDefinition_whenCreated_thenFieldsAreAccessible() {
        // Given / When
        var panel = new XYPanelDefinition("Bees vs Tick", "bees", "tick", "value");

        // Then
        assertThat(panel.title()).isEqualTo("Bees vs Tick");
        assertThat(panel.measurement()).isEqualTo("bees");
        assertThat(panel.xField()).isEqualTo("tick");
        assertThat(panel.yField()).isEqualTo("value");
    }

    @Test
    public void givenTwoEqualXYPanels_whenCompared_thenEqual() {
        // Given
        var panel1 = new XYPanelDefinition("A", "m", "x", "y");
        var panel2 = new XYPanelDefinition("A", "m", "x", "y");

        // When / Then
        assertThat(panel1).as("record equality").isEqualTo(panel2);
    }
}
