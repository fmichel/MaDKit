/**
 * 
 */
package madkit.networking.boot;

import static test.util.JUnitBooterAgent.COMMUNITY;
import static test.util.JUnitBooterAgent.GROUP;
import static test.util.JUnitBooterAgent.ROLE;

import java.util.logging.Level;

import madkit.kernel.Agent;
import madkit.kernel.Madkit;
import madkit.kernel.Message;

/**
 * @author fab
 *
 */
public class PingAgentMKInstance extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3108879338474646561L;

	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractAgent#activate()
	 */
	@Override
	protected void activate() {
		setLogLevel(Level.ALL);
		createGroup(COMMUNITY, GROUP, true, null);
		requestRole(COMMUNITY, GROUP, ROLE);
	}
	
	/* (non-Javadoc)
	 * @see madkit.kernel.Agent#live()
	 */
	@Override
	protected void live() {
		while (true) {
			sendReply(waitNextMessage(), new Message());
		}
	}
	
	public static void main(String[] args) {
		System.err.println(PingAgentMKInstance.class.getName());
//		Process p = Runtime.getRuntime().exec("java -cp bin madkit.kernel.Madkit --network --launchAgents "+PingAgentMKInstance.class.getClass().toString());
		new Thread(new Runnable() {			
			public void run() {
		String[] args = {"bin","--agentLogLevel","INFO","--MadkitLogLevel","OFF","--orgLogLevel","OFF","--network","--launchAgents",PingAgentMKInstance.class.getName()};
		Madkit.main(args);
		}
	}).start();
	}
}
