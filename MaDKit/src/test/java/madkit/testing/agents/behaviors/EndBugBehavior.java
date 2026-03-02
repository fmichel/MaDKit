package madkit.testing.agents.behaviors;

import madkit.kernel.TestAgentSupport;

/**
 *
 *
 */
public interface EndBugBehavior extends TestAgentSupport {

	@Override
	default void behaviorInEnd() {
		bug();
	}

}
