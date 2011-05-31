/**
 * 
 */
package madkit.agentLifeCycle.kill;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIME_OUT;
import static org.junit.Assert.assertEquals;
import madkit.kernel.Agent;
import madkit.testing.util.agent.DoItDuringLifeCycleAgent;
import test.util.JUnitBooterAgent;

/**
 * @author fab
 *
 */
public class  KillingWaitingMessageThreadedAgentsTest extends JUnitBooterAgent{

	private static final long serialVersionUID = -4842116743691032201L;

	/* (non-Javadoc)
	 * @see test.utils.JUnitBooterAgent#activate()
	 */
	@Override
	public void activate() {
		// TODO Auto-generated method stub
		super.activate();
		Agent a;
		a = new WaitingMessageAgent(true,false,false);
		assertEquals(TIME_OUT, launchAgent(a,1));
		assertEquals(SUCCESS, killAgent(a));
		assertAgentIsTerminated(a);

		a = new WaitingMessageAgent(false,true,false);
		assertEquals(SUCCESS, launchAgent(a));
		pause(100);
		assertEquals(SUCCESS, killAgent(a));
		assertAgentIsTerminated(a);

		a = new WaitingMessageAgent(false,false,true);
		assertEquals(SUCCESS, launchAgent(a));
		pause(100);
		assertEquals(SUCCESS, killAgent(a,1));
		assertAgentIsTerminated(a);

		a = new WaitingMessageAgent(true,false,true);
		assertEquals(TIME_OUT, launchAgent(a,1));
		assertEquals(SUCCESS, killAgent(a,1));
		assertAgentIsTerminated(a);

		a = new WaitingMessageAgent(true,true,true);
		assertEquals(TIME_OUT, launchAgent(a,1));
		assertEquals(SUCCESS, killAgent(a,1));
		assertAgentIsTerminated(a);

		/////////////////////////
		if(logger != null)
			logger.info("\n\n\n/////////////////// Starting BRUTAL KILLS ////////////////////////////////");
		a = new WaitingMessageAgent(true,false,false);
		assertEquals(TIME_OUT, launchAgent(a,1));
		assertEquals(SUCCESS, killAgent(a,0));
		assertAgentIsTerminated(a);

		a = new WaitingMessageAgent(false,true,false);
		assertEquals(SUCCESS, launchAgent(a));
		pause(100);
		assertEquals(SUCCESS, killAgent(a,0));
		assertAgentIsTerminated(a);

		a = new WaitingMessageAgent(false,false,true);
		assertEquals(SUCCESS, launchAgent(a));
		pause(100);
		assertEquals(SUCCESS, killAgent(a,0));
		assertAgentIsTerminated(a);

		a = new WaitingMessageAgent(true,false,true);
		assertEquals(TIME_OUT, launchAgent(a,1));
		assertEquals(SUCCESS, killAgent(a,0));
		assertAgentIsTerminated(a);

		a = new WaitingMessageAgent(true,true,true);
		assertEquals(TIME_OUT, launchAgent(a,1));
		assertEquals(SUCCESS, killAgent(a,0));
		assertAgentIsTerminated(a);

	}
	
}


class WaitingMessageAgent extends DoItDuringLifeCycleAgent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6447448286398006781L;

	/**
	 * @param inActivate
	 * @param inLive
	 * @param inEnd
	 */
	public WaitingMessageAgent(boolean inActivate, boolean inLive, boolean inEnd) {
		super(inActivate, inLive, inEnd);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doIt() {		waitNextMessage();	}

}

