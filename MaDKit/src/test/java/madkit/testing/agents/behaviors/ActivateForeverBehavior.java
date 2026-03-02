package madkit.testing.agents.behaviors;

import madkit.kernel.TestAgentSupport;

/**
 *
 *
 */
public interface ActivateForeverBehavior extends TestAgentSupport {

	@Override
	default void behaviorInActivate() {
		computeForEver();
	}

}
