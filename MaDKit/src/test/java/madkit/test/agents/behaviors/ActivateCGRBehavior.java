package madkit.test.agents.behaviors;

import madkit.kernel.TestAgentSupport;

/**
 *
 *
 */
public interface ActivateCGRBehavior extends TestAgentSupport {

	@Override
	default void orgInActivate() {
		takeDefaultLocalCGR();
	}

}