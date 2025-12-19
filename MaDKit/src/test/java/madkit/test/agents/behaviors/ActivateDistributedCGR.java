package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 *
 *
 */
public interface ActivateDistributedCGR extends TestHelpAgent {

	@Override
	default void orgInActivate() {
		createDefaultDistributedCGR();
	}

}
