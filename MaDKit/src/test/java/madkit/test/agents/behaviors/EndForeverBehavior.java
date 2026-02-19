package madkit.test.agents.behaviors;

import madkit.kernel.TestAgentSupport;

/**
 *
 *
 */
public interface EndForeverBehavior extends TestAgentSupport {

	@Override
	default void behaviorInEnd() {
		computeForEver();
	}

}