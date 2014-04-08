package madkit.testing.util.agent;

import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;

import java.util.logging.Level;

import madkit.kernel.Agent;
import madkit.kernel.Message;


public class ForEverSendRepliesWithTheSameMessageAgent extends Agent {

	public ForEverSendRepliesWithTheSameMessageAgent() {
		createGUIOnStartUp();
	}
	@Override
	protected void activate() {
		setLogLevel(Level.ALL);
		createGroup(COMMUNITY, GROUP,true);
		requestRole(COMMUNITY, GROUP, ROLE);
		sendMessage(COMMUNITY,GROUP,ROLE,new Message());
//		setLogLevel(Level.ALL);
	}
	
	@Override
	protected void live() {
		while (true) {
			Message m = waitNextMessage();
			logger.info(""+m.getSender().isFrom(getKernelAddress()));
			pause(100);
			sendReply(m, m);
		}
	}
}
