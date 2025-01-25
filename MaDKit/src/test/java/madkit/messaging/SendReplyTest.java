package madkit.messaging;

import static madkit.kernel.Agent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static madkit.kernel.MadkitUnitTestCase.COMMUNITY;
import static madkit.kernel.MadkitUnitTestCase.GROUP;
import static madkit.kernel.MadkitUnitTestCase.ROLE;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import madkit.kernel.Agent;
import madkit.kernel.Agent.ReturnCode;
import madkit.kernel.MadkitUnitTestCase;
import madkit.kernel.Message;
import madkit.test.agents.RequestRoleAgent;

/**
 *
 * @since MaDKit 5.0.0.15
 * @version 0.91
 * 
 */

public class SendReplyTest extends MadkitUnitTestCase {

	@Test
	public void returnNotInGroup() {
		launchTestedAgent(new Replier() {

			protected void onActivation() {
				super.onActivation();
				threadAssertEquals(SUCCESS, leaveRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(NOT_IN_GROUP, reply(new Message(), nextMessage()));
			}
		});
	}

	@Test
	public void returnInvalidAA() {
		launchTestedAgent(new Replier() {

			protected void onActivation() {
				super.onActivation();
				target.leaveGroup(COMMUNITY, GROUP);
				threadAssertEquals(INVALID_AGENT_ADDRESS, reply(new Message(), nextMessage()));
			}
		});
	}

	@Test
	public void returnSuccess() {
		launchTestedAgent(new Replier() {

			protected void onActivation() {
				super.onActivation();
				threadAssertEquals(SUCCESS, reply(new Message(), nextMessage()));
			}
		});
	}

	@Test
	public void nullArg() {
		launchTestedAgent(new Replier() {

			protected void onActivation() {
				try {
					threadAssertEquals(SUCCESS, reply(null, nextMessage()));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);

		launchTestedAgent(new Replier() {

			protected void onActivation() {
				try {
					threadAssertEquals(SUCCESS, reply(new Message(), null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);

		launchTestedAgent(new Replier() {

			protected void onActivation() {
				try {
					threadAssertEquals(SUCCESS, reply(null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

}

class Replier extends Agent {

	protected Agent target;

	protected void onActivation() {
		assertEquals(SUCCESS, launchAgent(target = new RequestRoleAgent()));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		assertEquals(SUCCESS, target.send(new Message(), COMMUNITY, GROUP, ROLE));
	}

}