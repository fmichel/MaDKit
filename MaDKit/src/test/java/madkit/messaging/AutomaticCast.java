package madkit.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.Message;
import madkit.messages.IntegerMessage;
import madkit.messages.StringMessage;
import madkit.test.agents.CGRAgent;

/**
 *
 *
 */
public class AutomaticCast extends MadkitConcurrentTestCase {

	@Test
	public void castSuccess() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(launchAgent(new ForEverReplierAgent(StringMessage.class))).as("launch replier")
						.isEqualTo(SUCCESS);
				send(new Message(), COMMUNITY, GROUP, ROLE);
				StringMessage m = waitNextMessage();
				assertThat(m).as("received string message").isNotNull();
				getLogger().info(m.toString());
				resume();
			}
		});
	}

	@Test
	public void castFailure() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				super.behaviorInActivate();
				assertThat(launchAgent(new ForEverReplierAgent(StringMessage.class))).as("launch replier")
						.isEqualTo(SUCCESS);
				send(new Message(), COMMUNITY, GROUP, ROLE);
				try {
					IntegerMessage m = waitNextMessage();
					getLogger().info(m.toString());
					noExceptionFailure();
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
				resume();
			}
		});
	}

	@Test
	public void nextMatches() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				super.behaviorInActivate();
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("b"));
				receiveMessage(new StringMessage("c"));
				List<StringMessage> l = getMailbox().nextMatches(StringMessage.class::isInstance);
				getLogger().info(l.toString());
				assertThat(l.size()).as("nextMatches size").isEqualTo(3);
				resume();
			}
		});
	}

	@Test
	public void nextMatchesFilterSuccess() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				super.behaviorInActivate();
				receiveMessage(new StringMessage("a"));
				receiveMessage(new StringMessage("b"));
				receiveMessage(new IntegerMessage());
				List<StringMessage> l = getMailbox().nextMatches(m -> m instanceof StringMessage);
				getLogger().info(l.toString());
				assertThat(l.size()).as("nextMatches filter size").isEqualTo(2);
				resume();
			}
		});
	}

}