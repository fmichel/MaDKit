package madkit.testing.util.agent;

import static madkit.kernel.JunitMadKit.COMMUNITY;
import static madkit.kernel.JunitMadKit.GROUP;
import static madkit.kernel.JunitMadKit.ROLE;

import java.util.Arrays;

import madkit.kernel.Agent;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit;
import madkit.message.StringMessage;

public class PongAgent extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void activate() {
		createGroupIfAbsent(JunitMadKit.COMMUNITY, GROUP, true, null);
		requestRole(COMMUNITY, GROUP, ROLE, null);
	}

	@Override
	public void live() {
		while (true) {
			pause(500);
			sendMessage(COMMUNITY, GROUP, ROLE, new StringMessage("test"));
			if (logger != null)
				logger.talk("\nreceived: " + nextMessage());
		}
	}

	@Override
	protected void end() {
		if (logger != null)
			logger.info("bye");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null) {
			args = Arrays.asList("--network", "--agentLogLevel", "ALL", "--launchAgents", PongAgent.class.getName(), ",true")
					.toArray(new String[0]);
		}
		Madkit.main(args);
	}

}
