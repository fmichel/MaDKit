package madkit.testing.agents.behaviors;

import madkit.kernel.TestAgentSupport;

/**
 *
 *
 */
public interface ActivateDistributedCGRBehavior extends TestAgentSupport {

	@Override
	default void orgInActivate() {
		takeDefaultDistributedCGR();
	}

}
