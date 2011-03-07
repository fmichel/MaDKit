/**
 * 
 */
package madkit.agentLifeCycle.kill.killcycle;

import static org.junit.Assert.assertEquals;
import static madkit.kernel.AbstractAgent.ReturnCode.*;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import test.util.JUnitBooterAgent;

/**
 * @author fab
 *
 */
public class  AgentKillCycleTest extends JUnitBooterAgent{


	/**
	 * 
	 */
	private static final long serialVersionUID = 447079274695245458L;

	@Override
	public void activate() {
		printTestTitle();
		createGroup(COMMUNITY, GROUP);
		Killer a = new Killer();
		Killer b = new Killer();
		a.setTarget(b);
		b.setTarget(a);
		launchAgent(a);
		launchAgent(b);
		assertEquals(SUCCESS,killAgent(a));
		assertEquals(ALREADY_KILLED,killAgent(a));
	}
	
	@Override
	protected void end() {
		if(logger != null)
			logger.info("ending");
		super.end();
	}

	class Killer extends Agent{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8166408913298822325L;
		AbstractAgent target;
		/**
		 * @param target the target to set
		 */
		final void setTarget(AbstractAgent target) {
			this.target = target;
		}
		@Override
		protected void activate() {
			requestRole(COMMUNITY, GROUP, ROLE);
		}
		
		@Override
		protected void live() {
			waitNextMessage();
		}
		
		@Override
		protected void end() {
			killAgent(target);
			killAgent(target);
		}
		
}

	
}

