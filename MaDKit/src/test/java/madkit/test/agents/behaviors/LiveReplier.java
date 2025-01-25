package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 *
 *
 */
public interface LiveReplier extends TestHelpAgent {

	@Override
	default void behaviorInLive() {
		while (true) {
			sleep(100);
			waitMessageAndReply();
		}
	}

}
