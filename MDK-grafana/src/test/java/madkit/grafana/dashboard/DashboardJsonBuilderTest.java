package madkit.grafana.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.testng.annotations.Test;

public class DashboardJsonBuilderTest {

	@Test
	public void givenTitleAndPanels_whenBuild_thenJsonContainsTitleAndPanels() {
		// Given
		var builder = new DashboardJsonBuilder();
		var panels = List.of(new PanelDefinition("Pop", "timeseries", "population", "value"),
				new PanelDefinition("Count", "stat", "population", "value"));

		// When
		String json = builder.build("Test Dashboard", panels);

		// Then
		assertThat(json).as("JSON should contain dashboard title").contains("\"title\":\"Test Dashboard\"");
		assertThat(json).as("JSON should contain panel titles").contains("\"title\":\"Pop\"")
				.contains("\"title\":\"Count\"");
		assertThat(json).as("JSON should contain panel types").contains("\"type\":\"timeseries\"")
				.contains("\"type\":\"stat\"");
	}

	@Test
	public void givenPanels_whenBuild_thenFluxQueriesArePresent() {
		// Given
		var builder = new DashboardJsonBuilder();
		var panels = List.of(new PanelDefinition("P", "timeseries", "cpu", "usage"));

		// When
		String json = builder.build("D", panels);

		// Then
		assertThat(json).as("JSON should contain Flux query referencing measurement")
				.contains("r._measurement == \\\"cpu\\\"");
		assertThat(json).as("JSON should contain Flux query referencing field").contains("r._field == \\\"usage\\\"");
	}

	@Test
	public void givenEmptyPanels_whenBuild_thenJsonHasEmptyPanelsArray() {
		// Given
		var builder = new DashboardJsonBuilder();

		// When
		String json = builder.build("Empty", List.of());

		// Then
		assertThat(json).as("JSON should have empty panels array").contains("\"panels\":[]");
	}

	@Test
	public void givenBuild_whenCalled_thenJsonContainsRefreshAndTimeRange() {
		// Given
		var builder = new DashboardJsonBuilder();

		// When
		String json = builder.build("T", List.of());

		// Then
		assertThat(json).as("JSON should configure 2s auto-refresh").contains("\"refresh\":\"2s\"");
		assertThat(json).as("JSON should have a default time range").contains("\"from\":\"now-5m\"");
	}

	@Test
	public void givenMultiplePanels_whenBuild_thenGridPositionsAreAutoCalculated() {
		// Given
		var builder = new DashboardJsonBuilder();
		var panels = List.of(new PanelDefinition("A", "timeseries", "m", "f"),
				new PanelDefinition("B", "timeseries", "m", "f"), new PanelDefinition("C", "timeseries", "m", "f"));

		// When
		String json = builder.build("Grid", panels);

		// Then — panels should have different gridPos x/y values
		assertThat(json).as("first panel x=0").contains("\"x\":0");
		assertThat(json).as("second panel x=12").contains("\"x\":12");
	}

	@Test
	public void givenTitle_whenBuild_thenJsonContainsDeterministicUid() {
		// Given
		var builder = new DashboardJsonBuilder();

		// When
		String json = builder.build("Bee Simulation", List.of());

		// Then
		assertThat(json).as("JSON should contain a UID derived from the title").contains("\"uid\":\"bee-simulation\"");
	}

	@Test
	public void givenPanels_whenBuild_thenDatasourceUidReferencesInfluxDbMadkit() {
		// Given
		var builder = new DashboardJsonBuilder();
		var panels = List.of(new PanelDefinition("P", "timeseries", "m", "f"));

		// When
		String json = builder.build("D", panels);

		// Then
		assertThat(json).as("panels should reference the provisioned datasource UID")
				.contains("\"uid\":\"influxdb-madkit\"");
	}

	@Test
	public void givenCustomRefreshInterval_whenBuild_thenJsonContainsCustomRefresh() {
		// Given
		var builder = new DashboardJsonBuilder();

		// When
		String json = builder.build("T", List.of(), "500ms");

		// Then
		assertThat(json).as("JSON should contain custom refresh interval").contains("\"refresh\":\"500ms\"");
	}

	@Test
	public void givenXYPanel_whenBuild_thenTypeIsXychart() {
		// Given
		var builder = new DashboardJsonBuilder();
		var panels = List.of(new XYPanelDefinition("XY", "sim", "tick", "value"));

		// When
		String json = builder.build("D", panels);

		// Then
		assertThat(json).as("JSON should contain xychart panel type").contains("\"type\":\"xychart\"");
	}

	@Test
	public void givenXYPanel_whenBuild_thenFluxQueryContainsPivot() {
		// Given
		var builder = new DashboardJsonBuilder();
		var panels = List.of(new XYPanelDefinition("XY", "sim", "tick", "value"));

		// When
		String json = builder.build("D", panels);

		// Then
		assertThat(json).as("Flux query should contain pivot transformation").contains("pivot");
	}

	@Test
	public void givenXYPanel_whenBuild_thenFluxQueryFiltersBothFields() {
		// Given
		var builder = new DashboardJsonBuilder();
		var panels = List.of(new XYPanelDefinition("XY", "sim", "tick", "value"));

		// When
		String json = builder.build("D", panels);

		// Then
		assertThat(json).as("Flux query should filter on xField").contains("r._field == \\\"tick\\\"");
		assertThat(json).as("Flux query should filter on yField").contains("r._field == \\\"value\\\"");
	}

	@Test
	public void givenXYPanel_whenBuild_thenOptionsContainsSeriesConfig() {
		// Given
		var builder = new DashboardJsonBuilder();
		var panels = List.of(new XYPanelDefinition("XY", "sim", "tick", "value"));

		// When
		String json = builder.build("D", panels);

		// Then
		assertThat(json).as("JSON should contain manual series mapping").contains("\"seriesMapping\":\"manual\"");
		assertThat(json).as("JSON should contain frame matcher selecting first frame")
				.contains("\"frame\":{\"matcher\":{\"id\":\"byIndex\",\"options\":0}}");
		assertThat(json).as("JSON should contain x field matcher with byName")
				.contains("\"x\":{\"field\":{\"matcher\":{\"id\":\"byName\",\"options\":\"tick\"}}}");
		assertThat(json).as("JSON should contain y field matcher with byName")
				.contains("\"y\":{\"field\":{\"matcher\":{\"id\":\"byName\",\"options\":\"value\"}}}");
	}

	@Test
	public void givenXYPanel_whenBuild_thenFieldConfigPresent() {
		// Given
		var builder = new DashboardJsonBuilder();
		var panels = List.of(new XYPanelDefinition("XY", "sim", "tick", "value"));

		// When
		String json = builder.build("D", panels);

		// Then
		assertThat(json).as("XY chart panel should include fieldConfig with defaults and overrides")
				.contains("\"fieldConfig\":{\"defaults\":{},\"overrides\":[]}");
	}

	@Test
	public void givenXYPanelWithCustomFields_whenBuild_thenMatcherOptionsReflectFields() {
		// Given
		var builder = new DashboardJsonBuilder();
		var panels = List.of(new XYPanelDefinition("Custom", "metrics", "step", "count"));

		// When
		String json = builder.build("D", panels);

		// Then — verify custom field names propagate into the matcher options
		assertThat(json).as("x matcher options should use the xField name")
				.contains("\"x\":{\"field\":{\"matcher\":{\"id\":\"byName\",\"options\":\"step\"}}}");
		assertThat(json).as("y matcher options should use the yField name")
				.contains("\"y\":{\"field\":{\"matcher\":{\"id\":\"byName\",\"options\":\"count\"}}}");
	}

	@Test
	public void givenMixedPanels_whenBuild_thenBothTypesRendered() {
		// Given
		var builder = new DashboardJsonBuilder();
		List<PanelDef> panels = List.of(new PanelDefinition("Series", "timeseries", "cpu", "usage"),
				new XYPanelDefinition("XY", "sim", "tick", "value"));

		// When
		String json = builder.build("Mixed", panels);

		// Then
		assertThat(json).as("JSON should contain timeseries panel type").contains("\"type\":\"timeseries\"");
		assertThat(json).as("JSON should contain xychart panel type").contains("\"type\":\"xychart\"");
	}
}
