package madkit.test.agents.behaviors;

import madkit.kernel.TestAgentSupport;

/**
 *
 *
 */
public interface OnLiveReplierBehavior extends TestAgentSupport {

	@Override
	default void behaviorInLive() {
		while (true) {
			waitMessageAndReply();
		}
	}

}