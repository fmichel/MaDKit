/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_ROLE;
import static madkit.kernel.Agent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.agr.SystemRoles;
import madkit.kernel.AgentAddress;
import madkit.kernel.DefaultTestAgent;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.Message;
import madkit.testing.agents.CGRAgent;

/**
 *
 * @version 6.0.4
 * 
 */

public class SendMessageConcurrentTest extends MadkitConcurrentTestCase {

	@Test
	public void givenAgent_whenReturnSuccess_thenSuccess() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				CGRAgent target = new CGRAgent();
				assertThat(launchAgent(target)).as("launchAgent return code").isEqualTo(SUCCESS);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("requestRole return code").isEqualTo(SUCCESS);

				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertThat(aa).as("agent address").isNotNull();

				// Without role
				assertThat(sendWithRole(new Message(), aa, null)).as("sendWithRole without role").isEqualTo(SUCCESS);
				Message m = target.nextMessage();
				assertThat(m).as("received message").isNotNull();
				assertThat(m.getReceiver().getRole()).as("receiver role").isEqualTo(ROLE);

				// With role
				assertThat(sendWithRole(new Message(), aa, ROLE)).as("sendWithRole with role").isEqualTo(SUCCESS);
				m = target.nextMessage();
				assertThat(m).as("received message after with-role").isNotNull();
				assertThat(m.getReceiver().getRole()).as("receiver role after with-role").isEqualTo(ROLE);
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenReturnSuccessOnCandidateRole_thenSuccess() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				CGRAgent target = new CGRAgent();
				assertThat(launchAgent(target)).as("launchAgent return code").isEqualTo(SUCCESS);

				// Without role
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER);
				assertThat(aa).as("agent address for group manager").isNotNull();
				assertThat(sendWithRole(new Message(), aa, null)).as("sendWithRole without role to manager")
						.isEqualTo(SUCCESS);
				Message m = target.nextMessage();
				assertThat(m).as("received message").isNotNull();
				assertThat(m.getReceiver().getRole()).as("receiver role").isEqualTo(SystemRoles.GROUP_MANAGER);
				assertThat(m.getSender().getRole()).as("sender role").isEqualTo(SystemRoles.GROUP_CANDIDATE);

				// With role
				aa = getAgentWithRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER);
				assertThat(aa).as("agent address for group manager (2)").isNotNull();
				assertThat(sendWithRole(new Message(), aa, SystemRoles.GROUP_CANDIDATE))
						.as("sendWithRole with candidate role").isEqualTo(SUCCESS);
				m = target.nextMessage();
				assertThat(m).as("received message after with-role").isNotNull();
				assertThat(m.getReceiver().getRole()).as("receiver role").isEqualTo(SystemRoles.GROUP_MANAGER);
				assertThat(m.getSender().getRole()).as("sender role").isEqualTo(SystemRoles.GROUP_CANDIDATE);
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenReturnInvalidAA_thenInvalidAgentAddress() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				CGRAgent target = new CGRAgent();
				assertThat(launchAgent(target)).as("launchAgent return code").isEqualTo(SUCCESS);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("requestRole return code").isEqualTo(SUCCESS);
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertThat(target.leaveRole(COMMUNITY, GROUP, ROLE)).as("target leave role").isEqualTo(SUCCESS);
				assertThat(send(new Message(), aa)).as("send to invalid aa").isEqualTo(INVALID_AGENT_ADDRESS);

				// With role
				assertThat(sendWithRole(new Message(), aa, ROLE)).as("sendWithRole to invalid aa")
						.isEqualTo(INVALID_AGENT_ADDRESS);
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenReturnNotInGroup_thenNotInGroup() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				CGRAgent target = new CGRAgent();
				assertThat(launchAgent(target)).as("launchAgent return code").isEqualTo(SUCCESS);
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertThat(sendWithRole(new Message(), aa, ROLE)).as("sendWithRole when not in group")
						.isEqualTo(NOT_IN_GROUP);
				assertThat(target.leaveRole(COMMUNITY, GROUP, ROLE)).as("target leave role").isEqualTo(SUCCESS);
				assertThat(send(new Message(), aa)).as("send after leave role").isEqualTo(INVALID_AGENT_ADDRESS);
				assertThat(send(new Message(), COMMUNITY, GROUP, ROLE)).as("send to role when not in group")
						.isEqualTo(NOT_ROLE);
				resume();

				// With role
			}
		});
	}

	@Test
	public void givenAgent_whenReturnRoleNotHandled_thenRoleNotHandled() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				CGRAgent target = new CGRAgent();
				assertThat(launchAgent(target)).as("launchAgent return code").isEqualTo(SUCCESS);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("requestRole return code").isEqualTo(SUCCESS);

				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertThat(sendWithRole(new Message(), aa, cgrDontExist())).as("sendWithRole role not handled")
						.isEqualTo(ROLE_NOT_HANDLED);
				resume();

			}
		});
	}

	@Test
	public void givenAgent_whenNullArgs_thenHandleNullPointerException() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				try {
					send(null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenNullAA_thenHandleNullPointerException() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				try {
					send(new Message(), null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenNullMessage_thenHandleNullPointerException() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				CGRAgent target = new CGRAgent();
				assertThat(launchAgent(target)).as("launchAgent return code").isEqualTo(SUCCESS);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("requestRole return code").isEqualTo(SUCCESS);
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				try {
					send(null, aa);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				resume();
			}
		});
	}

}
