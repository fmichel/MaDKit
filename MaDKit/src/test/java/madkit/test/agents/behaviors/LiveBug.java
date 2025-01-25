package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 *
 *
 */
public interface LiveBug extends TestHelpAgent {

	@Override
	default void behaviorInEnd() {
		bug();
	}

}
