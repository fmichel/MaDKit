package madkit.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.Agent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.Agent.ReturnCode.NOT_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_ROLE;
import static madkit.kernel.Agent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.Agent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.agr.SystemRoles;
import madkit.kernel.AgentAddress;
import madkit.kernel.DefaultTestAgent;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.Message;
import madkit.test.agents.CGRAgent;

/**
 *
 * @version 6.0.5
 * 
 */

public class SendWithCGRTest extends MadkitConcurrentTestCase {

	@Test
	public void givenAgentWithRole_whenSend_thenSuccessAndCorrectReceiver() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				CGRAgent target = new CGRAgent();
				launchAgent(target);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).isEqualTo(SUCCESS);

				// Without role
				assertThat(send(new Message(), COMMUNITY, GROUP, ROLE)).isEqualTo(SUCCESS);
				Message m = target.nextMessage();
				assertThat(m).isNotNull();
				assertThat(m.getReceiver().getRole()).isEqualTo(ROLE);

				// With role
				assertThat(sendWithRole(new Message(), COMMUNITY, GROUP, ROLE, ROLE)).isEqualTo(SUCCESS);
				m = target.nextMessage();
				assertThat(m).isNotNull();
				assertThat(m.getReceiver().getRole()).isEqualTo(ROLE);
				resume();
			}
		});
	}

	@Test
	public void givenCandidateRole_whenSend_thenSuccessAndCorrectRoles() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				CGRAgent target = new CGRAgent();
				launchAgent(target);

				// Without role
				assertThat(send(new Message(), COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER)).isEqualTo(SUCCESS);
				Message m = target.nextMessage();
				assertThat(m).isNotNull();
				assertThat(m.getReceiver().getRole()).isEqualTo(SystemRoles.GROUP_MANAGER);
				assertThat(m.getSender().getRole()).isEqualTo(SystemRoles.GROUP_CANDIDATE);

				// With role
				assertThat(
						sendWithRole(new Message(), COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER, SystemRoles.GROUP_CANDIDATE))
								.isEqualTo(SUCCESS);
				m = target.nextMessage();
				assertThat(m).isNotNull();
				assertThat(m.getReceiver().getRole()).isEqualTo(SystemRoles.GROUP_MANAGER);
				assertThat(m.getSender().getRole()).isEqualTo(SystemRoles.GROUP_CANDIDATE);
				resume();
			}
		});
	}

	@Test
	public void givenAgentWithRole_whenSendToInvalidAddress_thenInvalidAgentAddressReturned() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				CGRAgent target = new CGRAgent();
				launchAgent(target);

				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).isEqualTo(SUCCESS);
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertThat(target.leaveRole(COMMUNITY, GROUP, ROLE)).isEqualTo(SUCCESS);
				assertThat(send(new Message(), aa)).isEqualTo(INVALID_AGENT_ADDRESS);
				// With role
				assertThat(sendWithRole(new Message(), aa, ROLE)).isEqualTo(INVALID_AGENT_ADDRESS);
				resume();
			}
		});
	}

	@Test
	public void givenAgentNotInGroup_whenSend_thenNotInGroupReturned() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				launchAgent(new CGRAgent());
				assertThat(send(new Message(), COMMUNITY, GROUP, ROLE)).isEqualTo(NOT_IN_GROUP);
				// With role
				assertThat(sendWithRole(new Message(), COMMUNITY, GROUP, ROLE, ROLE)).isEqualTo(NOT_IN_GROUP);
				resume();
			}
		});
	}

	@Test
	public void givenNonExistentCGR_whenSend_thenProperErrorReturned() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				launchAgent(new CGRAgent());
				assertThat(send(new Message(), cgrDontExist(), GROUP, ROLE)).isEqualTo(NOT_COMMUNITY);
				assertThat(send(new Message(), COMMUNITY, cgrDontExist(), ROLE)).isEqualTo(NOT_GROUP);
				assertThat(send(new Message(), COMMUNITY, GROUP, cgrDontExist())).isEqualTo(NOT_ROLE);

				// With role
				assertThat(sendWithRole(new Message(), cgrDontExist(), GROUP, ROLE, ROLE)).isEqualTo(NOT_COMMUNITY);
				assertThat(sendWithRole(new Message(), COMMUNITY, cgrDontExist(), ROLE, ROLE)).isEqualTo(NOT_GROUP);
				assertThat(sendWithRole(new Message(), COMMUNITY, GROUP, cgrDontExist(), ROLE)).isEqualTo(NOT_ROLE);
				resume();
			}
		});
	}

	@Test
	public void givenRoleNotHandled_whenSendWithRole_thenRoleNotHandledReturned() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				launchAgent(new CGRAgent());
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).isEqualTo(SUCCESS);
				assertThat(sendWithRole(new Message(), COMMUNITY, GROUP, ROLE, cgrDontExist())).isEqualTo(ROLE_NOT_HANDLED);
				resume();
			}
		});
	}

	@Test
	public void givenNoRecipient_whenSend_thenNoRecipientFoundReturned() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				CGRAgent target = new CGRAgent();
				launchAgent(target);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).isEqualTo(SUCCESS);
				assertThat(target.leaveRole(COMMUNITY, GROUP, ROLE)).isEqualTo(SUCCESS);
				assertThat(send(new Message(), COMMUNITY, GROUP, ROLE)).isEqualTo(NO_RECIPIENT_FOUND);
				assertThat(sendWithRole(new Message(), COMMUNITY, GROUP, ROLE, ROLE)).isEqualTo(NO_RECIPIENT_FOUND);
				resume();
			}
		});
	}

	@Test
	public void givenNullCommunity_whenSend_thenThrowsNullPointerException() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				launchAgent(new CGRAgent());
				assertThatThrownBy(() -> send(new Message(), null, cgrDontExist(), cgrDontExist()))
						.isInstanceOf(NullPointerException.class);
				resume();
			}
		});
	}

	@Test
	public void givenNullGroup_whenSend_thenThrowsNullPointerException() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				launchAgent(new CGRAgent());
				assertThatThrownBy(() -> send(new Message(), COMMUNITY, null, cgrDontExist()))
						.isInstanceOf(NullPointerException.class);
				resume();
			}
		});
	}

	@Test
	public void givenNullRole_whenSend_thenThrowsNullPointerException() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				launchAgent(new CGRAgent());
				assertThatThrownBy(() -> send(new Message(), COMMUNITY, GROUP, null))
						.isInstanceOf(NullPointerException.class);
				resume();
			}
		});
	}

	@Test
	public void givenNullMessage_whenSend_thenThrowsNullPointerException() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				launchAgent(new CGRAgent());
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).isEqualTo(SUCCESS);
				assertThatThrownBy(() -> send(null, COMMUNITY, GROUP, ROLE)).isInstanceOf(NullPointerException.class);
				resume();
			}
		});
	}

}