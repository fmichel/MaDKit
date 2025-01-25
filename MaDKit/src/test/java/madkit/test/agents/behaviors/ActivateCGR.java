package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 *
 *
 */
public interface ActivateCGR extends TestHelpAgent {

	@Override
	default void orgInActivate() {
		createDefaultCGR();
	}

}
