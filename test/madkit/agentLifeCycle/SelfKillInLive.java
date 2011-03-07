/**
 * 
 */
package madkit.agentLifeCycle;

/**
 * @author fab
 *
 */
public class SelfKillInLive extends LifeCycleTestAgent{


	/**
	 * 
	 */
	private static final long serialVersionUID = -4833273509483327751L;

	public void live() {
		killAgent(this);
		for (int i = 0; i < 10000; i++) {
			System.err.print("doing in activate "+i);
		}
	}
}


