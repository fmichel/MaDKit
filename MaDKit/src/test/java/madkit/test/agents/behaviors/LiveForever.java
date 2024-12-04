package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 * @author Fabien Michel
 *
 */
public interface LiveForever extends TestHelpAgent {

	@Override
	default void behaviorInLive() {
		computeForEver();
	}

}
