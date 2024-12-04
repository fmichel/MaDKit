package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 * @author Fabien Michel
 *
 */
public interface LiveBug extends TestHelpAgent {

	@Override
	default void behaviorInEnd() {
		bug();
	}

}
