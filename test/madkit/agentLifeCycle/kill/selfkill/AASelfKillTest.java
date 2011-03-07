/**
 * 
 */
package madkit.agentLifeCycle.kill.selfkill;

import static org.junit.Assert.assertEquals;
import static madkit.kernel.AbstractAgent.ReturnCode.*;
import madkit.kernel.AbstractAgent;
import test.util.JUnitBooterAgent;

/**
 * @author fab
 *
 */
public class  AASelfKillTest extends JUnitBooterAgent{


	/**
	 * 
	 */
	private static final long serialVersionUID = -4538557814046610477L;

	@Override
	public void activate() {
		SelfKillAA a = new SelfKillAA();
		launchAgent(a);
		assertEquals(SUCCESS,a.selfDestruct());
		assertEquals(ALREADY_KILLED,killAgent(a));
		assertEquals(ALREADY_KILLED,a.selfDestruct());
		if(logger != null)
			logger.info("ending test");
	}
	
	@Override
	protected void end() {
		if(logger != null)
			logger.info("ending");
		super.end();
	}

}

class SelfKillAA extends AbstractAgent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6316054974112022575L;

	public ReturnCode selfDestruct(){
		ReturnCode r = killAgent(this);
		requestRole("test", "test", "test");
		return r;
	}

}

