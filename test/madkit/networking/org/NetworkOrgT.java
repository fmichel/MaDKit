/**
 * 
 */
package madkit.networking.org;

import madkit.kernel.AgentAddress;
import madkit.kernel.Madkit;
import madkit.kernel.Message;
import test.util.JUnitBooterAgent;
import test.util.OrgTestAgent;
/**
 * @author fab
 *
 */
public class NetworkOrgT extends JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1777643377200214547L;


	@Override
	public void activate() {
		launchAgent(new OrgMessageAgentTest());
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


class OrgMessageAgentTest extends OrgTestAgent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4061685499633455393L;

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