package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 *
 *
 */
public interface EndBug extends TestHelpAgent {

	@Override
	default void behaviorInLive() {
		bug();
	}

}
