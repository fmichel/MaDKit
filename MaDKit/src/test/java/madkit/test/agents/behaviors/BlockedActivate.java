package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 *
 *
 */
public interface BlockedActivate extends TestHelpAgent {

	@Override
	default void behaviorInActivate() {
		blockForever();
	}

}
