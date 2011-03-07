/**
 * 
 */
package madkit.networking.boot;

import static org.junit.Assert.assertEquals;
import madkit.kernel.Madkit;
import madkit.kernel.NetworkAgent;
import test.util.JUnitBooterAgent;
/**
 * @author fab
 *
 */
public class NetworkBootTest extends JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7689915953731530340L;

	@Override
	public void activate() {
		System.err.println(getAgentsWithRole(NetworkAgent.NETWORK_COMMUNITY, NetworkAgent.NETWORK_GROUP, "net agent"));
		assertEquals(1, getAgentsWithRole(NetworkAgent.NETWORK_COMMUNITY, NetworkAgent.NETWORK_GROUP, "net agent").size());
	}
	
	/* (non-Javadoc)
	 * @see test.utils.JUnitBooterAgent#madkitInit()
	 */
	@Override
	public void madkitInit() {
		String[] args = {"--network","--agentLogLevel","FINEST","--"+Madkit.MadkitLogLevel,"OFF","--orgLogLevel","OFF","--launchAgents",getClass().getName()};
		Madkit.main(args);
	}
}
