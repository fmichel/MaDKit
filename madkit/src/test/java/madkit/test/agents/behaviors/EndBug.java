package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 * @author Fabien Michel
 *
 */
public interface EndBug extends TestHelpAgent {
	
	@Override
	default void behaviorInLive() {
		bug();
	}

}
