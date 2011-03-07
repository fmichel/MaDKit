/**
 * 
 */
package madkit.agentLifeCycle;

/**
 * @author fab
 *
 */
public class SelfKillInActivate extends LifeCycleTestAgent{


	/**
	 * 
	 */
	private static final long serialVersionUID = -7449172124770174432L;

	public void activate() {
		killAgent(this);
		for (int i = 0; i < 10000; i++) {
			requestRole("test", "t", "r", null);
			System.err.print("doing in activate "+i);
		}
	}
	
	/* (non-Javadoc)
	 * @see test.madkit.agentLifeCycle.LifeCycleTestAgent#end()
	 */
	@Override
	public void end() {
		// TODO Auto-generated method stub
		super.end();
		killAgent(this);
		for (int i = 0; i < 10000; i++) {
			requestRole("test", "t", "r", null);
			System.err.print("doing in activate "+i);
		}
	}
}


