package madkit.test.agents.behaviors;

import madkit.kernel.TestHelpAgent;

/**
 * @author Fabien Michel
 *
 */
public interface ActivateCGR extends TestHelpAgent {
	
	@Override
	default void orgInActivate() {
		createDefaultCGR();
	}

}
