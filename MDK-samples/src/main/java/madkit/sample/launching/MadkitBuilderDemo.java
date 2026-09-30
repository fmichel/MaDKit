/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit
 *******************************************************************************/
package madkit.sample.launching;

import java.util.logging.Level;

import madkit.kernel.Madkit;

/**
 * Minimal programmatic MaDKit launch.
 * <p>
 * Unlike the command-line examples, this sample uses typed builder methods and passes an
 * agent class directly to {@link madkit.kernel.MadkitBuilder#launchAgent(Class)}.
 * </p>
 *
 * <p>Run this class from a graphical or headless environment. The kernel remains available
 * after the short-lived sample agent has activated; stop the application with the usual
 * MaDKit exit action.</p>
 */
public final class MadkitBuilderDemo {

	private MadkitBuilderDemo() {
		// Utility class.
	}

	/**
	 * Starts MaDKit with a headless configuration and one typed agent.
	 *
	 * @param args ignored; configuration is expressed by the builder
	 */
	public static void main(String[] args) {
		Madkit.builder()
				.headless(true)
				.agentLogLevel(Level.INFO)
				.launchAgent(HelloMaDKit.class)
				.build();
	}
}