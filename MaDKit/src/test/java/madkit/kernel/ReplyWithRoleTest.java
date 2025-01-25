package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import org.testng.annotations.Test;

import madkit.kernel.Agent.ReturnCode;
import madkit.messages.StringMessage;
import madkit.messaging.ForEverReplierAgent;
import madkit.test.agents.CGRAgent;

/**
 *
 * @since MaDKit 5.0.0.15
 * @version 0.9
 * 
 */

public class ReplyWithRoleTest extends JunitMadkit {

	@Test
	public void ROLE_NOT_HANDLED() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				threadAssertEquals(SUCCESS, launchAgent(new ForEverReplierAgent(StringMessage.class)));
				send(new Message(), COMMUNITY, GROUP, ROLE);
				Message waitNextMessage = waitNextMessage();
				threadAssertEquals(SUCCESS, leaveRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(ROLE_NOT_HANDLED, replyWithRole(new Message(), waitNextMessage, ROLE));
			}
		});
	}

	@Test
	public void NOT_IN_GROUP() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				threadAssertEquals(SUCCESS, launchAgent(new ForEverReplierAgent(StringMessage.class)));
				send(new Message(), COMMUNITY, GROUP, ROLE);
				Message waitNextMessage = waitNextMessage();
				threadAssertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_IN_GROUP, replyWithRole(new Message(), waitNextMessage, ROLE));
			}
		});
	}

	@Test
	public void INVALID_AGENT_ADDRESS() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				ForEverReplierAgent target;
				threadAssertEquals(SUCCESS, launchAgent(target = new ForEverReplierAgent(StringMessage.class)));
				send(new Message(), COMMUNITY, GROUP, ROLE);
				Message waitNextMessage = waitNextMessage();
				target.leaveGroup(COMMUNITY, GROUP);
				threadAssertEquals(INVALID_AGENT_ADDRESS, replyWithRole(new Message(), waitNextMessage, ROLE));
			}
		});
	}

	@Test
	public void returnSuccess() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				threadAssertEquals(SUCCESS, launchAgent(new ForEverReplierAgent()));
				send(new Message(), COMMUNITY, GROUP, ROLE);
				threadAssertEquals(SUCCESS, replyWithRole(new Message(), waitNextMessage(), ROLE));
			}
		});
	}

	@Test
	public void wrongArg() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				threadAssertEquals(ReturnCode.CANT_REPLY, replyWithRole(new Message(), new Message(), ROLE));
			}
		});
	}

	@Test
	public void wrongArgFromMessageSentFromAnObject() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				receiveMessage(new Message());
				threadAssertEquals(ReturnCode.CANT_REPLY, replyWithRole(new Message(), nextMessage(), ROLE));
			}
		});
	}

	@Test
	public void nullArg() {
		launchTestedAgent(new CGRAgent() {

			protected void onActivation() {
				try {
					threadAssertEquals(SUCCESS, replyWithRole(null, nextMessage(), ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);

		launchTestedAgent(new CGRAgent() {

			protected void onActivation() {
				try {
					threadAssertEquals(SUCCESS, replyWithRole(new Message(), null, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);

	}

}
