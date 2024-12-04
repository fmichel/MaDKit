package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 * @author Fabien Michel
 *
 */
public interface ActivateBug extends TestHelpAgent {

	@Override
	default void behaviorInActivate() {
		bug();
	}

}
