package madkit.kernel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.logging.Level;

import org.testng.annotations.Test;

public class MadkitBuilderTest {

	@Test
	public void givenNewBuilder_whenDefaultsAreRead_thenStandardConfigurationIsApplied() {
		// Given
		MadkitBuilder builder = Madkit.builder();

		// When
		KernelConfig config = builder.toKernelConfig();

		// Then
		assertThat(config.getAgentLogLevel()).isEqualTo(Level.INFO);
		assertThat(config.getKernelLogLevel()).isEqualTo(Level.OFF);
		assertThat(config.getMadkitLogLevel()).isEqualTo(Level.INFO);
		assertThat(config.isHeadless()).isFalse();
		assertThat(config.isNetworkEnabled()).isFalse();
		assertThat(config.getSeed()).isEqualTo(Integer.MIN_VALUE);
		assertThat(config.getAgents()).isEmpty();
	}

	@Test
	public void givenBuilder_whenTypedOptionsAreSet_thenConfigurationContainsThem() {
		// Given
		MadkitBuilder builder = Madkit.builder().agentLogLevel(Level.FINE).kernelLogLevel(Level.WARNING)
				.madkitLogLevel(Level.SEVERE).headless(true).network(true).noLog(true).noRandomizedFields(true)
				.createLogFiles(true).autoStart(true).seed(42).logDirectory("logs");

		// When
		KernelConfig config = builder.toKernelConfig();

		// Then
		assertThat(config.getAgentLogLevel()).isEqualTo(Level.FINE);
		assertThat(config.getKernelLogLevel()).isEqualTo(Level.WARNING);
		assertThat(config.getMadkitLogLevel()).isEqualTo(Level.SEVERE);
		assertThat(config.isHeadless()).isTrue();
		assertThat(config.isNetworkEnabled()).isTrue();
		assertThat(config.isNoLog()).isTrue();
		assertThat(config.isNoRandomizedFields()).isTrue();
		assertThat(config.isCreateLogFiles()).isTrue();
		assertThat(config.isAutoStart()).isTrue();
		assertThat(config.getSeed()).isEqualTo(42);
		assertThat(config.getLogDirectory()).isEqualTo("logs");
	}

	@Test
	public void givenBuilder_whenAgentsAndViewersAreAdded_thenAllClassNamesArePreserved() {
		// Given
		MadkitBuilder builder = Madkit.builder().launchAgent("example.FirstAgent").launchAgent("example.SecondAgent")
				.viewer("example.FirstViewer").viewer("example.SecondViewer")
				.scheduler(madkit.simulation.scheduler.TickBasedScheduler.class)
				.environment(madkit.simulation.SimuEnvironment.class).model(madkit.simulation.SimuModel.class);

		// When
		KernelConfig config = builder.toKernelConfig();

		// Then
		assertThat(config.getAgents()).containsExactly("example.FirstAgent", "example.SecondAgent");
		assertThat(config.getViewers()).containsExactly("example.FirstViewer", "example.SecondViewer");
		assertThat(config.getScheduler()).isEqualTo(madkit.simulation.scheduler.TickBasedScheduler.class.getName());
		assertThat(config.getEnvironment()).isEqualTo(madkit.simulation.SimuEnvironment.class.getName());
		assertThat(config.getModel()).isEqualTo(madkit.simulation.SimuModel.class.getName());
	}

	@Test
	public void givenHeadlessBuilder_whenBuilt_thenMadkitKernelIsRunning() {
		// Given
		MadkitBuilder builder = Madkit.builder().headless(true).madkitLogLevel(Level.OFF);

		// When
		Madkit madkit = builder.build();

		// Then
		assertThat(madkit.getConfig().isHeadless()).isTrue();
		assertThat(madkit.getConfig().getMadkitLogLevel()).isEqualTo(Level.OFF);
	}
}