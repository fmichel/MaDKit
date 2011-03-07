/**
 * 
 */
package madkit.networking.boot;

import static org.junit.Assert.*;
import madkit.kernel.Agent;
import madkit.kernel.Madkit;
import madkit.kernel.Message;
import madkit.kernel.NetworkAgent;

import org.junit.Test;

import test.util.JUnitBooterAgent;
/**
 * @author fab
 *
 */
public class NetworkStartTest extends JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3621235612679345140L;

	@Override
	public void activate() {
		super.activate();
		assertTrue(isRole(NetworkAgent.NETWORK_COMMUNITY, NetworkAgent.NETWORK_GROUP, NetworkAgent.NETWORK_ROLE));
		assertTrue(isRole(COMMUNITY, GROUP, ROLE));
		Agent a = new Agent(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 470929593689001172L;
			protected void activate() {
				requestRole(COMMUNITY, GROUP, ROLE+" TT");
			}
			protected void live() {
				Message m =	sendMessageAndWaitForReply(COMMUNITY, GROUP, ROLE, new Message());
				if(logger != null)
					logger.info("\n\n\n---------------received "+m);
			}
		};
		launchAgent(a);
		pause(2000);
	}
	
//	public void launchMKNetworkInstance() {
//		new Thread(new Runnable() {			
//			@Override
//			public void run() {
//		String[] args = {"--"+MadkitCommandLineOptions.autoAgentLogDirectory,getBinTestDir(),"--agentLogLevel","OFF","--MadkitLogLevel","OFF","--orgLogLevel","OFF","--network","--launchAgents","test.util.ForEverAgent"};
//		Madkit.main(args);
//		}
//	}).start();
//
//	}
//	
//	
	@Test
	public void madkitInit() {
		PingAgentMKInstance.main(null);
		pause(2000);
		super.madkitInit();
	}


}
