package madkit.test.agents.behaviors;

import madkit.kernel.TestAgentSupport;

/**
 *
 *
 */
public interface ComputeForeverInLive extends TestAgentSupport {

	@Override
	default void behaviorInLive() {
		computeForEver();
	}

}