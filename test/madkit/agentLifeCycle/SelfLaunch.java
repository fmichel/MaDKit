/**
 * 
 */
package madkit.agentLifeCycle;

import static org.junit.Assert.assertEquals;
import madkit.testing.util.agent.DoItDuringLifeCycleAgent;

/**
 * @author fab
 *
 */
public class SelfLaunch extends DoItDuringLifeCycleAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7890046389013758692L;


	/**
	 * @param inActivate
	 * @param inLive
	 * @param inEnd
	 */
	public SelfLaunch(boolean inActivate, boolean inLive, boolean inEnd) {
		super(inActivate, inLive, inEnd);
		// TODO Auto-generated constructor stub
	}

	public SelfLaunch(boolean inActivate, boolean inLive) {
		super(inActivate, inLive, false);
	}

	public SelfLaunch(boolean inActivate) {
		super(inActivate, false, false);
	}


	/* (non-Javadoc)
	 * @see test.madkit.agentLifeCycle.DoItDuringLifeCycleAgent#doIt()
	 */
	@Override
	public void doIt() {
		assertEquals(ReturnCode.ALREADY_LAUNCHED,launchAgent(this));
	}

	
}


