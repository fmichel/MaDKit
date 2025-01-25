
package madkit.messaging;

import static madkit.kernel.Agent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.Agent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.Agent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.Agent.ReturnCode.NOT_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_ROLE;
import static madkit.kernel.Agent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.Agent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import org.testng.annotations.Test;

import madkit.agr.SystemRoles;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.MadkitUnitTestCase;
import madkit.kernel.Message;
import madkit.test.agents.RequestRoleAgent;

/**
 *
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class SendWithCGRTest extends MadkitUnitTestCase {

	@Test
	public void returnSuccess() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));

				// Without role
				threadAssertEquals(SUCCESS, send(new Message(), COMMUNITY, GROUP, ROLE));
				Message m = target.nextMessage();
				threadAssertNotNull(m);
				threadAssertEquals(ROLE, m.getReceiver().getRole());

				// With role
				threadAssertEquals(SUCCESS, sendWithRole(new Message(), COMMUNITY, GROUP, ROLE, ROLE));
				m = target.nextMessage();
				threadAssertNotNull(m);
				threadAssertEquals(ROLE, m.getReceiver().getRole());
			}
		});
	}

	@Test
	public void returnSuccessOnCandidateRole() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));

				// Without role
				threadAssertEquals(SUCCESS, send(new Message(), COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER));
				Message m = target.nextMessage();
				threadAssertNotNull(m);
				threadAssertEquals(SystemRoles.GROUP_MANAGER, m.getReceiver().getRole());
				threadAssertEquals(SystemRoles.GROUP_CANDIDATE, m.getSender().getRole());

				// With role
				threadAssertEquals(SUCCESS, sendWithRole(new Message(), COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER,
						SystemRoles.GROUP_CANDIDATE));
				m = target.nextMessage();
				threadAssertNotNull(m);
				threadAssertEquals(SystemRoles.GROUP_MANAGER, m.getReceiver().getRole());
				threadAssertEquals(SystemRoles.GROUP_CANDIDATE, m.getSender().getRole());
			}
		});
	}

	@Test
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

	@Test
	public void returnNotInGroup() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(NOT_IN_GROUP, send(new Message(), COMMUNITY, GROUP, ROLE));
				// With role
				threadAssertEquals(NOT_IN_GROUP, sendWithRole(new Message(), COMMUNITY, GROUP, ROLE, ROLE));
			}
		});
	}

	@Test
	public void returnNotCGR() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(NOT_COMMUNITY, send(new Message(), cgrDontExist(), GROUP, ROLE));
				threadAssertEquals(NOT_GROUP, send(new Message(), COMMUNITY, cgrDontExist(), ROLE));
				threadAssertEquals(NOT_ROLE, send(new Message(), COMMUNITY, GROUP, cgrDontExist()));

				// With role
				threadAssertEquals(NOT_COMMUNITY, sendWithRole(new Message(), cgrDontExist(), GROUP, ROLE, ROLE));
				threadAssertEquals(NOT_GROUP, sendWithRole(new Message(), COMMUNITY, cgrDontExist(), ROLE, ROLE));
				threadAssertEquals(NOT_ROLE, sendWithRole(new Message(), COMMUNITY, GROUP, cgrDontExist(), ROLE));
			}
		});
	}

	@Test
	public void returnRoleNotHandled() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(ROLE_NOT_HANDLED, sendWithRole(new Message(), COMMUNITY, GROUP, ROLE, cgrDontExist()));
			}
		});
	}

	@Test
	public void returnNoRecipientFound() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(NO_RECIPIENT_FOUND, send(new Message(), COMMUNITY, GROUP, ROLE));
				threadAssertEquals(NO_RECIPIENT_FOUND, sendWithRole(new Message(), COMMUNITY, GROUP, ROLE, ROLE));
			}
		});
	}

	@Test
	public void nullCommunity() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				try {
					send(new Message(), null, cgrDontExist(), cgrDontExist());
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

	@Test
	public void nullGroup() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				try {
					send(new Message(), COMMUNITY, null, cgrDontExist());
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

	@Test
	public void nullRole() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				try {
					send(new Message(), COMMUNITY, GROUP, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

	@Test
	public void nullMessage() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				try {
					send(null, COMMUNITY, GROUP, ROLE);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

}
