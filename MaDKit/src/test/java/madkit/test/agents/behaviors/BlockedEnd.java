package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 *
 *
 */
public interface BlockedEnd extends TestHelpAgent {

	@Override
	default void behaviorInEnd() {
		blockForever();
	}

}
