package madkit.test.agents.behaviors;

import madkit.kernel.TestAgentSupport;

/**
 *
 *
 */
public interface LiveForeverBehavior extends TestAgentSupport {

	@Override
	public default void behaviorInLive() {
		computeForEver();
	}

}