package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 *
 *
 */
public interface LiveForever extends TestHelpAgent {

	@Override
	default void behaviorInLive() {
		computeForEver();
	}

}
