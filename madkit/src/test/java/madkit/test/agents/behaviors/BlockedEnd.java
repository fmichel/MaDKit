package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 * @author Fabien Michel
 *
 */
public interface BlockedEnd extends TestHelpAgent {
	
	@Override
	default void behaviorInEnd() {
		blockForever();
	}

}
