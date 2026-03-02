package madkit.testing.agents.behaviors;

import madkit.kernel.TestAgentSupport;

/**
 *
 *
 */
public interface BlockedEndBehavior extends TestAgentSupport {

	@Override
	default void behaviorInEnd() {
		blockForever();
	}

}
