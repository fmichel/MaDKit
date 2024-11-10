package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 * @author Fabien Michel
 *
 */
public interface ActivateForever extends TestHelpAgent {

	@Override
	default void behaviorInActivate() {
		computeForEver();
	}

}
