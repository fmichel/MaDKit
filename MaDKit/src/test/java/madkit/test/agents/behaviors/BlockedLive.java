package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 *
 *
 */
public interface BlockedLive extends TestHelpAgent {

	@Override
	default void behaviorInLive() {
		blockForever();
	}

}
