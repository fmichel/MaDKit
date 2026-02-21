package madkit.simulation.viewer;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.GraphicsEnvironment;
import java.util.Map;

import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javafx.scene.chart.XYChart;
import madkit.kernel.Agent;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.Probe;
import madkit.test.support.DefaultSimuAgentTest;

/**
 * Integration test for {@link RolesPopulationLineChartDrawer}.
 */
public class RolesPopulationLineChartDrawerTest extends MadkitConcurrentTestCase {

	@BeforeMethod
	protected void checkEnvironment() {
		if (GraphicsEnvironment.isHeadless()) {
			throw new SkipException("Skipping tests because the environment is headless");
		}
	}

	@Test
	public void testRolesPopulationLineChartDrawer() {
		runSimuTest(new DefaultSimuAgentTest() {

			@Override
			public void behaviorInActivate() {
				// Create two roles in the model group
				playRole("roleA");
				playRole("roleB");

				// Launch the viewer that should detect the roles and create series
				RolesPopulationLineChartDrawer viewer = new RolesPopulationLineChartDrawer();
				assertThat(launchAgent(viewer)).as("launch viewer").isEqualTo(Agent.ReturnCode.SUCCESS);

				// The viewer should have created series for the two roles
				Map<Probe, XYChart.Series<String, Number>> series = viewer.getSeries();
				assertThat(series).as("series map").isNotEmpty();
				assertThat(series.values()).extracting(XYChart.Series::getName).contains("roleA", "roleB");

				// Trigger a display which should add one data point per monitored role
				viewer.display();
				boolean anyWithData = series.values().stream().anyMatch(s -> s.getData().size() > 0);
				assertThat(anyWithData).as("at least one serie got a data point after display").isTrue();

				resume();
			}
		});
	}

}