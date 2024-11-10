package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 * @author Fabien Michel
 *
 */
public interface BlockedLive extends TestHelpAgent {
	
	@Override
	default void behaviorInLive() {
		blockForever();
	}

}
