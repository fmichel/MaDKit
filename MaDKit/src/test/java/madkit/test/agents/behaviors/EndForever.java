package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 *
 *
 */
public interface EndForever extends TestHelpAgent {

	@Override
	default void behaviorInEnd() {
		computeForEver();
	}

}
