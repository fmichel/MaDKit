/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit
 *******************************************************************************/
package madkit.sample.launching;

import java.util.logging.Level;

import madkit.kernel.Madkit;
import madkit.sample.agent.basic.SimpleAgent;

/**
 * Builder configuration with several startup options.
 * <p>
 * This example shows that builder calls can be combined fluently. It also demonstrates both
 * class-based and class-name-based agent registration, which is useful when a class name comes
 * from application metadata.
 * </p>
 */
public final class MadkitBuilderOptionsDemo {

	private MadkitBuilderOptionsDemo() {
		// Utility class.
	}

	/**
	 * Starts MaDKit with two startup agents and explicit deterministic options.
	 *
	 * @param args ignored; configuration is expressed by the builder
	 */
	public static void main(String[] args) {
		Madkit.builder()
				.headless(true)
				.madkitLogLevel(Level.INFO)
				.agentLogLevel(Level.FINE)
				.noRandomizedFields(true)
				.seed(42)
				.launchAgent(SimpleAgent.class)
				.launchAgent("madkit.sample.launching.HelloMaDKit")
				.build();
	}
}