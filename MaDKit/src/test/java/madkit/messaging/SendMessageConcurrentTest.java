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

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_ROLE;
import static madkit.kernel.Agent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.agr.SystemRoles;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.Message;
import madkit.test.agents.RequestRoleAgent;

/**
 *
 * @version 6.0.4
 * 
 */

public class SendMessageConcurrentTest extends MadkitConcurrentTestCase {

	@Test
	public void givenAgent_whenReturnSuccess_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));

				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				threadAssertNotNull(aa);

				// Without role
				threadAssertEquals(SUCCESS, sendWithRole(new Message(), aa, null));
				Message m = target.nextMessage();
				threadAssertNotNull(m);
				threadAssertEquals(ROLE, m.getReceiver().getRole());

				// With role
				threadAssertEquals(SUCCESS, sendWithRole(new Message(), aa, ROLE));
				m = target.nextMessage();
				threadAssertNotNull(m);
				threadAssertEquals(ROLE, m.getReceiver().getRole());
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenReturnSuccessOnCandidateRole_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));

				// Without role
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER);
				threadAssertNotNull(aa);
				threadAssertEquals(SUCCESS, sendWithRole(new Message(), aa, null));
				Message m = target.nextMessage();
				threadAssertNotNull(m);
				threadAssertEquals(SystemRoles.GROUP_MANAGER, m.getReceiver().getRole());
				threadAssertEquals(SystemRoles.GROUP_CANDIDATE, m.getSender().getRole());

				// With role
				aa = getAgentWithRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER);
				threadAssertNotNull(aa);
				threadAssertEquals(SUCCESS, sendWithRole(new Message(), aa, SystemRoles.GROUP_CANDIDATE));
				m = target.nextMessage();
				threadAssertNotNull(m);
				threadAssertEquals(SystemRoles.GROUP_MANAGER, m.getReceiver().getRole());
				threadAssertEquals(SystemRoles.GROUP_CANDIDATE, m.getSender().getRole());
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenReturnInvalidAA_thenInvalidAgentAddress() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				threadAssertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(INVALID_AGENT_ADDRESS, send(new Message(), aa));

				// With role
				threadAssertEquals(INVALID_AGENT_ADDRESS, sendWithRole(new Message(), aa, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenReturnNotInGroup_thenNotInGroup() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				threadAssertEquals(NOT_IN_GROUP, sendWithRole(new Message(), aa, ROLE));
				threadAssertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(INVALID_AGENT_ADDRESS, send(new Message(), aa));
				threadAssertEquals(NOT_ROLE, send(new Message(), COMMUNITY, GROUP, ROLE));
				resume();

				// With role
			}
		});
	}

	@Test
	public void givenAgent_whenReturnRoleNotHandled_thenRoleNotHandled() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));

				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				threadAssertEquals(ROLE_NOT_HANDLED, sendWithRole(new Message(), aa, cgrDontExist()));
				resume();

			}
		});
	}

	@Test
	public void givenAgent_whenNullArgs_thenHandleNullPointerException() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
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
		runTest(new Agent() {
			@Override
			protected void onActivation() {
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
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				RequestRoleAgent target = new RequestRoleAgent();
				threadAssertEquals(SUCCESS, launchAgent(target));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
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
