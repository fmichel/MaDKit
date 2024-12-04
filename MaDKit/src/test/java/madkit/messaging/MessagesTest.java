package madkit.messaging;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import madkit.kernel.JunitMadkit;
import madkit.messages.IntegerMessage;
import madkit.messages.Messages;
import madkit.messages.StringMessage;
import madkit.test.agents.CGRAgent;

/**
 * @author Fabien Michel
 *
 */
public class MessagesTest extends JunitMadkit {

	@Test
	public void groupingBy() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("b"));
				receiveMessage(new StringMessage("c"));
				List<StringMessage> l = getMailbox().nextMatches(m -> m instanceof StringMessage);
				Map<String, List<StringMessage>> m = Messages.groupingByContent(l);
				getLogger().info(m.toString());
				threadAssertEquals(3, m.size());
			}
		});
	}

	@Test
	public void min() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("b"));
				receiveMessage(new StringMessage("c"));
				List<StringMessage> l = getMailbox().nextMatches(m -> m instanceof StringMessage);
				l = Messages.messagesWithMinContent(l);
				getLogger().info(l.toString());
				threadAssertEquals(3, l.size());
			}
		});
	}

	@Test
	public void max() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("b"));
				receiveMessage(new StringMessage("c"));
				List<StringMessage> l = getMailbox().nextMatches(m -> m instanceof StringMessage);
				l = Messages.messagesWithMaxContent(l);
				getLogger().info(l.toString());
				threadAssertEquals(1, l.size());
			}
		});
	}

	@Test
	public void average() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				for (int i = 0; i < 101; i++) {
					receiveMessage(new IntegerMessage(i));
				}
				List<IntegerMessage> l = getMailbox().nextMatches(m -> m instanceof IntegerMessage);
				double mean = Messages.averageOnContent(l);
				getLogger().info(String.valueOf(mean));
				threadAssertEquals(50.0, mean);
			}
		});
	}

}
