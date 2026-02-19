package madkit.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import madkit.kernel.DefaultTestAgent;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.messages.IntegerMessage;
import madkit.messages.Messages;
import madkit.messages.StringMessage;

/**
 * Refactored to use ConcurrentTestAgent and AssertJ assertions.
 */
public class MessagesTest extends MadkitConcurrentTestCase {

	@Test
	public void groupingBy() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("b"));
				receiveMessage(new StringMessage("c"));
				List<StringMessage> l = getMailbox().nextMatches(m -> m instanceof StringMessage);
				Map<String, List<StringMessage>> m = Messages.groupingByContent(l);
				getLogger().info(m.toString());
				assertThat(m.size()).isEqualTo(3);
				resume();
			}
		});
	}

	@Test
	public void min() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("b"));
				receiveMessage(new StringMessage("c"));
				List<StringMessage> l = getMailbox().nextMatches(m -> m instanceof StringMessage);
				l = Messages.messagesWithMinContent(l);
				getLogger().info(l.toString());
				assertThat(l.size()).isEqualTo(3);
				resume();
			}
		});
	}

	@Test
	public void max() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("b"));
				receiveMessage(new StringMessage("c"));
				List<StringMessage> l = getMailbox().nextMatches(m -> m instanceof StringMessage);
				l = Messages.messagesWithMaxContent(l);
				getLogger().info(l.toString());
				assertThat(l.size()).isEqualTo(1);
				resume();
			}
		});
	}

	@Test
	public void average() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				for (int i = 0; i < 101; i++) {
					receiveMessage(new IntegerMessage(i));
				}
				List<IntegerMessage> l = getMailbox().nextMatches(m -> m instanceof IntegerMessage);
				double mean = Messages.averageOnContent(l);
				getLogger().info(String.valueOf(mean));
				assertThat(mean).isEqualTo(50.0);
				resume();
			}
		});
	}

}