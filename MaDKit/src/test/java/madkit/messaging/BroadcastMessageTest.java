
package madkit.messaging;

import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.Agent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static org.testng.Assert.assertNotSame;

import java.util.Collections;

import org.testng.annotations.Test;

import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Message;
import madkit.messages.StringMessage;
import madkit.test.agents.RequestRoleAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class BroadcastMessageTest extends JunitMadkit {

	@Test
	public void returnSuccess() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				final Agent target = new RequestRoleAgent();
				final Agent target2 = new Agent() {
					protected void onActivation() {
						threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
					}
				};
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));

				// Without role
				getAgentsWithRole(COMMUNITY, GROUP, ROLE);
				threadAssertEquals(SUCCESS, broadcast(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE)));
				Message m = target.nextMessage();
				threadAssertEquals(SUCCESS,
						broadcast(new StringMessage("test"), getAgentsWithRole(COMMUNITY, GROUP, ROLE)));
				threadAssertNotNull(target.nextMessage());
				threadAssertEquals(ROLE, m.getReceiver().getRole());

				// With role
				threadAssertEquals(SUCCESS,
						broadcastWithRole(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE), ROLE));
				m = target.nextMessage();
				threadAssertNotNull(m);
				threadAssertEquals(ROLE, m.getReceiver().getRole());

				// verifying cloning
				launchAgent(target2);
				threadAssertEquals(SUCCESS,
						broadcastWithRole(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE), ROLE));
				m = target.nextMessage();
				threadAssertNotNull(m);
				Message m2 = target2.nextMessage();
				threadAssertEquals(ROLE, m2.getReceiver().getRole());
				threadAssertEquals(m.getConversationID(), m2.getConversationID());
				assertNotSame(m2, m);
			}
		});
	}

	@Test
	public void returnNotInGroup() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				final Agent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				// Without role
				threadAssertEquals(NOT_IN_GROUP, broadcast(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE)));

				// With role
				threadAssertEquals(NOT_IN_GROUP,
						broadcastWithRole(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE), ROLE));
			}
		});
	}

	@Test
	public void returnNotCGR() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				final Agent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(Collections.EMPTY_LIST, getAgentsWithRole(cgrDontExist(), GROUP, ROLE));
				threadAssertEquals(Collections.EMPTY_LIST, getAgentsWithRole(COMMUNITY, cgrDontExist(), ROLE));
				threadAssertEquals(Collections.EMPTY_LIST, getAgentsWithRole(COMMUNITY, GROUP, cgrDontExist()));

			}
		});
	}

	@Test
	public void returnRoleNotHandled() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				final Agent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(ROLE_NOT_HANDLED,
						broadcastWithRole(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE), cgrDontExist()));
			}
		});
	}

	@Test
	public void returnNoRecipientFound() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				final Agent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(NO_RECIPIENT_FOUND, broadcast(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE)));
				threadAssertEquals(NO_RECIPIENT_FOUND,
						broadcastWithRole(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE), ROLE));
			}
		});
	}

	@Test
	public void nullArgs() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				final Agent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				try {
					broadcast(null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
//				try {
//					broadcast(COMMUNITY, GROUP, null, null);
//					noExceptionFailure();
//				} catch (NullPointerException e) {
//					// e.printStackTrace();
//				}
//				try {
//					threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
//					broadcast(getAgentsWithRole(COMMUNITY, GROUP, ROLE), null);
//					noExceptionFailure();
//				} catch (NullPointerException e) {
//					// e.printStackTrace();
//				}
//				try {
//					broadcast(null, GROUP, ROLE, null);
//					noExceptionFailure();
//				} catch (NullPointerException e) {
//					// e.printStackTrace();
//				}
			}
		});
	}

}
