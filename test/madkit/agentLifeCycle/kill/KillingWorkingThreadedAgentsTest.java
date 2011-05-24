/**
 * 
 */
package madkit.agentLifeCycle.kill;

import static org.junit.Assert.assertEquals;
import madkit.kernel.Agent;
import madkit.testing.util.agent.DoItDuringLifeCycleAgent;
import static madkit.kernel.AbstractAgent.ReturnCode.*;

import test.util.JUnitBooterAgent;

/**
 * @author fab
 *
 */
public class  KillingWorkingThreadedAgentsTest extends JUnitBooterAgent{

	private static final long serialVersionUID = -6014720151024942255L;

	/**
	 * @see test.utils.JUnitBooterAgent#activate()
	 */
	@Override
	public void activate() {
		// TODO Auto-generated method stub
		super.activate();
		Agent a;
		a = new WorkingAgent(true,false,false);
		assertEquals(TIME_OUT, launchAgent(a,0));
		assertEquals(SUCCESS, killAgent(a,1));
		assertAgentIsTerminated(a);

		a = new WorkingAgent(false,true,false);
		assertEquals(SUCCESS, launchAgent(a));
		assertEquals(SUCCESS, killAgent(a,1));
		assertAgentIsTerminated(a);

		a = new WorkingAgent(false,false,true);
		assertEquals(SUCCESS, launchAgent(a));
		assertEquals(SUCCESS, killAgent(a,1));
		assertAgentIsTerminated(a);

		a = new WorkingAgent(true,false,true);
		assertEquals(TIME_OUT, launchAgent(a,1));
		assertEquals(SUCCESS, killAgent(a,1));
		assertAgentIsTerminated(a);

		a = new WorkingAgent(true,true,true);
		assertEquals(TIME_OUT, launchAgent(a,1));
		assertEquals(SUCCESS, killAgent(a,1));
		assertAgentIsTerminated(a);

		/////////////////////////
		if(logger != null)
			logger.info("\n\n\n/////////////////// Starting BRUTAL KILLS ////////////////////////////////");
		a = new WorkingAgent(true,false,false);
		assertEquals(TIME_OUT, launchAgent(a,1));
		assertEquals(SUCCESS, killAgent(a,0));
		assertAgentIsTerminated(a);

		a = new WorkingAgent(false,true,false);
		assertEquals(SUCCESS, launchAgent(a));
		pause(100);
		assertEquals(SUCCESS, killAgent(a,0));
		assertAgentIsTerminated(a);

		a = new WorkingAgent(false,false,true);
		assertEquals(SUCCESS, launchAgent(a));
		pause(100);
		assertEquals(SUCCESS, killAgent(a,0));
		assertAgentIsTerminated(a);

		a = new WorkingAgent(true,false,true);
		assertEquals(TIME_OUT, launchAgent(a,1));
		assertEquals(SUCCESS, killAgent(a,0));
		assertAgentIsTerminated(a);

		a = new WorkingAgent(true,true,true);
		assertEquals(TIME_OUT, launchAgent(a,1));
		assertEquals(SUCCESS, killAgent(a,0));
		assertAgentIsTerminated(a);

	}
	
}


class WorkingAgent extends DoItDuringLifeCycleAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -175432600448922540L;

	public WorkingAgent(boolean inActivate, boolean inLive, boolean inEnd) {
		super(inActivate, inLive, inEnd);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doIt() {		
		for (int i =0; i < 100000000;i++) {
			pause(1);
			double d = Math.random()*2;
			d*=Math.PI*100;
			getAgentWithRole("test", "test", "test");
		}
	}
}
