package madkit.messaging;

import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;
import static madkit.kernel.JunitMadkit.startTimer;
import static madkit.kernel.JunitMadkit.stopTimer;

import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Message;

/**
* @author Fabien Michel
*/
public class MessagingTest extends AbstractAgent {

	@Override
	protected void activate() {
		createGroup(COMMUNITY, GROUP);
		requestRole(COMMUNITY, GROUP, ROLE);
		AgentAddress a = getAgentsWithRole(COMMUNITY, GROUP, ROLE, true).get(0);
		for (int i = 0; i < 15; i++) {
			startTimer();
			for (int j = 0; j < 3_000_000; j++) {
				sendMessage(a, new Message());
			}
			purgeMailbox();
			stopTimer("message creation time: ");
		}
		launchAgentBucket(AbstractAgent.class.getName(),200_000, COMMUNITY+","+GROUP+","+ ROLE);
		for (int j = 0; j < 30; j++) {
			startTimer();
			broadcastMessage(COMMUNITY, GROUP, ROLE,new Message());
			stopTimer(" broadcast time: ");
		}
	}
	// 86 ms on beltegeuse

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		executeThisAgent(1,false);
	}

}
