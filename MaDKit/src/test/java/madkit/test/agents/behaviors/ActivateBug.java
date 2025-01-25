package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 *
 *
 */
public interface ActivateBug extends TestHelpAgent {

	@Override
	default void behaviorInActivate() {
		bug();
	}

}
