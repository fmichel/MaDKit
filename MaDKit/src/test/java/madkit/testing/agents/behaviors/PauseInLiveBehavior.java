package madkit.testing.agents.behaviors;

import madkit.kernel.TestAgentSupport;

/**
 *
 *
 */
public interface PauseInLiveBehavior extends TestAgentSupport {

	@Override
	default void behaviorInLive() {
		sleep(1000);
	}

}
