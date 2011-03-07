/**
 * 
 */
package madkit.networking;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import madkit.kernel.NetworkAgent;
import madkit.kernel.Madkit;
import test.util.JUnitBooterAgent;
/**
 * @author fab
 *
 */
public class DiscoverTest extends JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2285419134906033471L;


	@Override
	public void activate() {
		super.activate();
		launchMKNetworkInstance();		
		launchMKNetworkInstance();		
		launchMKNetworkInstance();	
		launchMKNetworkInstance();	
		launchMKNetworkInstance();	
		pause(10000);
		if(logger != null)
			logger.info(""+getAgentsWithRole(NetworkAgent.NETWORK_COMMUNITY, NetworkAgent.NETWORK_GROUP, NetworkAgent.NETWORK_ROLE));
		assertEquals(6, getAgentsWithRole(NetworkAgent.NETWORK_COMMUNITY, NetworkAgent.NETWORK_GROUP, NetworkAgent.NETWORK_ROLE).size());
	}
	
	public void launchMKNetworkInstance() {
		new Thread(new Runnable() {			
			@Override
			public void run() {
		String[] args = {getBinTestDir(),"--agentLogLevel","OFF","--MadkitLogLevel","OFF","--orgLogLevel","OFF","--network","--launchAgents","test.util.ForEverAgent"};
		Madkit.main(args);
		}
	}).start();

	}
	
	
	@Test
	public void madkitInit() {
		String[] args = { "--network","--agentLogLevel","ALL","--MadkitLogLevel","OFF","--orgLogLevel","OFF","--launchAgents",getClass().getName()};
		Madkit.main(args);
	}


}
