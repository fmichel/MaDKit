package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 * @author Fabien Michel
 *
 */
public interface BlockedActivate extends TestHelpAgent {
	
	@Override
	default void behaviorInActivate() {
		blockForever();
	}

}
