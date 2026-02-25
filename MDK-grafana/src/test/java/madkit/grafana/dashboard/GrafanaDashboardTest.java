package madkit.grafana.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.testng.annotations.Test;

public class GrafanaDashboardTest {

    /** A connection stub that captures the posted JSON. */
    static class StubConnection extends GrafanaConnection {
        String lastPostedJson;

        StubConnection() {
            super("http://fake:3000", "admin", "admin");
        }

        @Override
        public String postDashboard(String dashboardJson) {
            this.lastPostedJson = dashboardJson;
            return "/d/test-uid/test";
        }
    }

    @Test
    public void givenDashboardWithPanels_whenSave_thenJsonIsPostedToConnection() throws Exception {
        // Given
        var stub = new StubConnection();
        var dashboard = new GrafanaDashboard("Test", stub);
        dashboard.addTimeSeriesPanel("Series", "metric", "value");
        dashboard.addStatPanel("Current", "metric", "value");

        // When
        dashboard.save();

        // Then
        assertThat(stub.lastPostedJson)
                .as("JSON should be posted to connection")
                .isNotNull()
                .contains("\"title\":\"Test\"");
    }

    @Test
    public void givenDashboard_whenAddPanels_thenPanelListGrows() {
        // Given
        var stub = new StubConnection();
        var dashboard = new GrafanaDashboard("Test", stub);

        // When
        dashboard.addTimeSeriesPanel("A", "m", "f");
        dashboard.addStatPanel("B", "m", "f");
        dashboard.addTablePanel("C", "m");
        dashboard.addXYChartPanel("D", "m", "x", "y");

        // Then
        assertThat(dashboard.getPanels())
                .as("dashboard should have 4 panels")
                .hasSize(4);
    }

    @Test
    public void givenDashboard_whenExportJson_thenReturnsSameAsBuiltJson() {
        // Given
        var stub = new StubConnection();
        var dashboard = new GrafanaDashboard("Export", stub);
        dashboard.addTimeSeriesPanel("P", "m", "f");

        // When
        String json = dashboard.exportJson();

        // Then
        assertThat(json)
                .as("exported JSON should contain dashboard title")
                .contains("\"title\":\"Export\"");
    }

    @Test
    public void givenDashboard_whenSave_thenDashboardUrlIsCaptured() throws Exception {
        // Given
        var stub = new StubConnection();
        var dashboard = new GrafanaDashboard("Test", stub);
        dashboard.addTimeSeriesPanel("P", "m", "f");

        // When
        dashboard.save();

        // Then — openInBrowser should not throw (URL was captured)
        assertThatCode(dashboard::openInBrowser)
                .as("openInBrowser should not throw after save")
                .doesNotThrowAnyException();
    }

    @Test
    public void givenDashboard_whenSetRefreshInterval_thenExportJsonReflectsIt() {
        // Given
        var stub = new StubConnection();
        var dashboard = new GrafanaDashboard("Test", stub);
        dashboard.addTimeSeriesPanel("P", "m", "f");

        // When
        dashboard.setRefreshInterval("1s");
        String json = dashboard.exportJson();

        // Then
        assertThat(json).as("exported JSON should have 1s refresh").contains("\"refresh\":\"1s\"");
    }

    @Test
    public void givenDashboard_whenAddXYChartPanel_thenPanelListGrows() {
        // Given
        var stub = new StubConnection();
        var dashboard = new GrafanaDashboard("Test", stub);

        // When
        dashboard.addXYChartPanel("Bees vs Tick", "bees", "tick", "value");

        // Then
        assertThat(dashboard.getPanels())
                .as("dashboard should have 1 panel")
                .hasSize(1);
    }

    @Test
    public void givenDashboardWithXYPanel_whenSave_thenJsonContainsXychart() throws Exception {
        // Given
        var stub = new StubConnection();
        var dashboard = new GrafanaDashboard("Test", stub);
        dashboard.addXYChartPanel("Bees vs Tick", "bees", "tick", "value");

        // When
        dashboard.save();

        // Then
        assertThat(stub.lastPostedJson)
                .as("JSON should contain xychart panel type")
                .contains("\"type\":\"xychart\"");
    }

    @Test
    public void givenDashboardWithMixedPanels_whenExportJson_thenBothTypesPresent() {
        // Given
        var stub = new StubConnection();
        var dashboard = new GrafanaDashboard("Test", stub);
        dashboard.addTimeSeriesPanel("Series", "metric", "value");
        dashboard.addXYChartPanel("XY", "metric", "tick", "value");

        // When
        String json = dashboard.exportJson();

        // Then
        assertThat(json)
                .as("JSON should contain timeseries type")
                .contains("\"type\":\"timeseries\"");
        assertThat(json)
                .as("JSON should contain xychart type")
                .contains("\"type\":\"xychart\"");
    }
}
