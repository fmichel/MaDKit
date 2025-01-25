package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 *
 *
 */
public interface ActivateForever extends TestHelpAgent {

	@Override
	default void behaviorInActivate() {
		computeForEver();
	}

}
