/**
 * 
 */
package madkit.networking.messaging;

import madkit.kernel.AgentAddress;
import madkit.kernel.Madkit;
import madkit.kernel.Message;
import test.util.JUnitBooterAgent;
import test.util.OrgTestAgent;
/**
 * @author fab
 *
 */
public class NetworkMessagingTest extends JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2226317432181332931L;


	@Override
	public void activate() {
		launchAgent(new NetworkMessageAgentTest());
	}
	
	
	/* (non-Javadoc)
	 * @see test.utils.JUnitBooterAgent#madkitInit()
	 */
	@Override
	public void madkitInit() {
		String[] args = {"--network","--agentLogLevel","ALL","--"+Madkit.MadkitLogLevel,"ALL","--orgLogLevel","ALL","--launchAgents",getClass().getName()};
		Madkit.main(args);
	}
}


class NetworkMessageAgentTest extends OrgTestAgent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8456452767330561605L;

	/* (non-Javadoc)
	 * @see test.util.OrgTestAgent#activate()
	 */
	@Override
	protected void live() {
		AgentAddress aa = getAgentWithRole("test", "test", "test");
		if(aa == null)
			waitNextMessage();
		else
			sendMessage(aa, new Message());
	}
}