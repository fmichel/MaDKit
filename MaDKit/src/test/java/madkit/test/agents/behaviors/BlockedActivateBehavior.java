package madkit.test.agents.behaviors;

import madkit.kernel.TestAgentSupport;

/**
 *
 *
 */
public interface BlockedActivateBehavior extends TestAgentSupport {

	@Override
	default void behaviorInActivate() {
		blockForever();
	}

}