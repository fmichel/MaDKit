package madkit.kernel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.logging.Level;

import org.testng.annotations.Test;

public class MadkitStringLaunchTest {

	@Test
	public void givenLegacyStringArguments_whenMadkitIsConstructed_thenOptionsAreStillParsed() {
		// Given
		String agentClassName = StringLaunchAgent.class.getName();

		// When
		Madkit madkit = new Madkit("--headless", "--madkitLogLevel", "OFF", "--agentLogLevel", "FINE", "--agents",
				agentClassName);

		assertThat(madkit.getConfig().isHeadless()).isTrue();
		assertThat(madkit.getConfig().getMadkitLogLevel()).isEqualTo(Level.OFF);
		assertThat(madkit.getConfig().getAgentLogLevel()).isEqualTo(Level.FINE);
		assertThat(madkit.getConfig().getAgents()).containsExactly(agentClassName);
	}

	@Test
	public void givenStringAgentClass_whenBuilderIsBuilt_thenStringRegistrationRemainsSupported() {
		// Given
		String agentClassName = StringLaunchAgent.class.getName();

		// When
		Madkit madkit = Madkit.builder().headless(true).madkitLogLevel(Level.OFF).launchAgent(agentClassName).build();

		// Then
		assertThat(madkit.getConfig().getAgents()).containsExactly(agentClassName);
	}

	public static class StringLaunchAgent extends Agent {
		// A concrete agent used only to verify class-name based startup.
	}
}