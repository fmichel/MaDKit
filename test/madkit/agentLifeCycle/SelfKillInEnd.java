/**
 * 
 */
package madkit.agentLifeCycle;

import static org.junit.Assert.*;

/**
 * @author fab
 *
 */
public class SelfKillInEnd extends LifeCycleTestAgent{


	/**
	 * 
	 */
	private static final long serialVersionUID = -4503203815470981878L;

	public void end() {
		super.end();
		assertTrue(false);
		killAgent(this);
		for (int i = 0; i < 10000; i++) {
			assert false;
			System.err.print("doing in end "+i);
		}
	}
}


