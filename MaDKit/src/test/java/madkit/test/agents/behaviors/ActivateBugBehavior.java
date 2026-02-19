package madkit.test.agents.behaviors;

import madkit.kernel.TestAgentSupport;

/**
 *
 *
 */
public interface ActivateBugBehavior extends TestAgentSupport {

	@Override
	default void behaviorInActivate() {
		bug();
	}

}