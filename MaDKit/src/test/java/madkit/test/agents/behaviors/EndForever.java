package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 * @author Fabien Michel
 *
 */
public interface EndForever extends TestHelpAgent {

	@Override
	default void behaviorInEnd() {
		computeForEver();
	}

}
