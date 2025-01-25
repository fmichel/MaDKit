package madkit.messaging;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import java.util.List;

import org.testng.annotations.Test;

import madkit.kernel.JunitMadkit;
import madkit.kernel.Message;
import madkit.messages.IntegerMessage;
import madkit.messages.StringMessage;
import madkit.test.agents.CGRAgent;

/**
 *
 *
 */
public class AutomaticCast extends JunitMadkit {

	@Test
	public void castSuccess() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				threadAssertEquals(SUCCESS, launchAgent(new ForEverReplierAgent(StringMessage.class)));
				send(new Message(), COMMUNITY, GROUP, ROLE);
				StringMessage m = waitNextMessage();
				threadAssertNotNull(m);
				getLogger().info(m.toString());
			}
		});
	}

	@Test
	public void castFailure() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				threadAssertEquals(SUCCESS, launchAgent(new ForEverReplierAgent(StringMessage.class)));
				send(new Message(), COMMUNITY, GROUP, ROLE);
				try {
					IntegerMessage m = waitNextMessage();
					getLogger().info(m.toString());
					noExceptionFailure();
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void nextMatches() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("b"));
				receiveMessage(new StringMessage("c"));
				List<StringMessage> l = getMailbox().nextMatches(StringMessage.class::isInstance);
				getLogger().info(l.toString());
				threadAssertEquals(3, l.size());
			}
		});
	}

	@Test
	public void nextMatchesFilterSuccess() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("b"));
				receiveMessage(new IntegerMessage());
				List<StringMessage> l = getMailbox().nextMatches(m -> m instanceof StringMessage);
				getLogger().info(l.toString());
				threadAssertEquals(2, l.size());
			}
		});
	}

}
