package madkit.messaging;

import static madkit.kernel.Agent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_ROLE;
import static madkit.kernel.Agent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import madkit.agr.SystemRoles;
import madkit.kernel.Agent;
import madkit.kernel.Agent.ReturnCode;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Message;
import madkit.test.agents.RequestRoleAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class SendMessageTest extends JunitMadkit {

	@Test
	public void returnSuccess() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));

				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertNotNull(aa);

				// Without role
				threadAssertEquals(SUCCESS, sendWithRole(new Message(), aa, null));
				Message m = target.nextMessage();
				assertNotNull(m);
				threadAssertEquals(ROLE, m.getReceiver().getRole());

				// With role
				threadAssertEquals(SUCCESS, sendWithRole(new Message(), aa, ROLE));
				m = target.nextMessage();
				assertNotNull(m);
				threadAssertEquals(ROLE, m.getReceiver().getRole());
			}
		});
	}

	@Test(groups = { "messaging", "checkintest" })
	public void returnSuccessOnCandidateRole() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));

				// Without role
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER_ROLE);
				assertNotNull(aa);
				threadAssertEquals(SUCCESS, sendWithRole(new Message(), aa, null));
				Message m = target.nextMessage();
				assertNotNull(m);
				threadAssertEquals(SystemRoles.GROUP_MANAGER_ROLE, m.getReceiver().getRole());
				threadAssertEquals(SystemRoles.GROUP_CANDIDATE_ROLE, m.getSender().getRole());

				// With role
				aa = getAgentWithRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER_ROLE);
				assertNotNull(aa);
				threadAssertEquals(SUCCESS, sendWithRole(new Message(), aa, SystemRoles.GROUP_CANDIDATE_ROLE));
				m = target.nextMessage();
				assertNotNull(m);
				threadAssertEquals(SystemRoles.GROUP_MANAGER_ROLE, m.getReceiver().getRole());
				threadAssertEquals(SystemRoles.GROUP_CANDIDATE_ROLE, m.getSender().getRole());
			}
		});
	}

	@Test(groups = { "messaging", "checkintest" })
	public void returnInvalidAA() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				threadAssertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(INVALID_AGENT_ADDRESS, send(new Message(), aa));

				// With role
				threadAssertEquals(INVALID_AGENT_ADDRESS, sendWithRole(new Message(), aa, ROLE));
			}
		});
	}

	@Test(groups = { "messaging", "checkintest" })
	public void returnNotInGroup() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				threadAssertEquals(NOT_IN_GROUP, sendWithRole(new Message(), aa, ROLE));
				threadAssertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(INVALID_AGENT_ADDRESS, send(new Message(), aa));
				threadAssertEquals(NOT_ROLE, send(new Message(), COMMUNITY, GROUP, ROLE));

				// With role
			}
		});
	}

	@Test(groups = { "messaging", "checkintest" })
	public void returnRoleNotHandled() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));

				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				threadAssertEquals(ROLE_NOT_HANDLED, sendWithRole(new Message(), aa, cgrDontExist()));

			}
		});
	}

	@Test(groups = { "messaging", "checkintest" })
	public void nullArgs() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				try {
					send(null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test(groups = { "messaging", "checkintest" })
	public void nullAA() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				try {
					send(new Message(), null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test(groups = { "messaging", "checkintest" })
	public void nullMessage() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				try {
					send(null, aa);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

}
