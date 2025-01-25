package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 *
 *
 */
public interface PauseInLive extends TestHelpAgent {
	@Override
	default void behaviorInLive() {
		getLogger().finer("PAUSING IN LIVE....");
		sleep(10000);
	}

}
