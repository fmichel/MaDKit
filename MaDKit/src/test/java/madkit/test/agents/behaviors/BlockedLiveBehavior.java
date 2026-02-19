package madkit.test.agents.behaviors;

import madkit.kernel.TestAgentSupport;

/**
 *
 *
 */
public interface BlockedLiveBehavior extends TestAgentSupport {

	@Override
	default void behaviorInLive() {
		blockForever();
	}

}