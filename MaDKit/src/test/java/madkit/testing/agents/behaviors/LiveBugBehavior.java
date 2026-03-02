package madkit.testing.agents.behaviors;

import madkit.kernel.TestAgentSupport;

/**
 *
 *
 */
public interface LiveBugBehavior extends TestAgentSupport {

	@Override
	public default void behaviorInLive() {
		bug();
	}

}
