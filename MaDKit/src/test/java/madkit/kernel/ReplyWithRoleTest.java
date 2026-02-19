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

package madkit.kernel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.kernel.Agent.ReturnCode;
import madkit.messages.StringMessage;
import madkit.messaging.ForEverReplierAgent;
import madkit.test.agents.CGRAgent;

/**
 * The Class ReplyWithRoleConcurrentTest.
 * 
 * @version 6.0.5
 */
public class ReplyWithRoleTest extends MadkitConcurrentTestCase {

	@Test
	public void givenAgent_whenRoleNotHandled_thenReturnRoleNotHandled() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(launchAgent(new ForEverReplierAgent(StringMessage.class))).as("launchAgent return code")
						.isEqualTo(SUCCESS);
				send(new Message(), COMMUNITY, GROUP, ROLE);
				Message waitNextMessage = waitNextMessage();
				assertThat(leaveRole(COMMUNITY, GROUP, ROLE)).as("leaveRole return code").isEqualTo(SUCCESS);
				assertThat(replyWithRole(new Message(), waitNextMessage, ROLE)).as("replyWithRole when role not handled")
						.isEqualTo(ROLE_NOT_HANDLED);
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenNotInGroup_thenReturnNotInGroup() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(launchAgent(new ForEverReplierAgent(StringMessage.class))).as("launchAgent return code")
						.isEqualTo(SUCCESS);
				send(new Message(), COMMUNITY, GROUP, ROLE);
				Message waitNextMessage = waitNextMessage();
				assertThat(leaveGroup(COMMUNITY, GROUP)).as("leaveGroup return code").isEqualTo(SUCCESS);
				assertThat(replyWithRole(new Message(), waitNextMessage, ROLE)).as("replyWithRole when not in group")
						.isEqualTo(NOT_IN_GROUP);
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenInvalidAgentAddress_thenReturnInvalidAgentAddress() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				ForEverReplierAgent target;
				assertThat(launchAgent(target = new ForEverReplierAgent(StringMessage.class))).as("launchAgent return code")
						.isEqualTo(SUCCESS);
				send(new Message(), COMMUNITY, GROUP, ROLE);
				Message waitNextMessage = waitNextMessage();
				target.leaveGroup(COMMUNITY, GROUP);
				assertThat(replyWithRole(new Message(), waitNextMessage, ROLE))
						.as("replyWithRole with invalid agent address").isEqualTo(INVALID_AGENT_ADDRESS);
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenReplyWithRole_thenReturnSuccess() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(launchAgent(new ForEverReplierAgent())).as("launchAgent return code").isEqualTo(SUCCESS);
				send(new Message(), COMMUNITY, GROUP, ROLE);
				assertThat(replyWithRole(new Message(), waitNextMessage(), ROLE)).as("replyWithRole should return SUCCESS")
						.isEqualTo(SUCCESS);
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenWrongArg_thenReturnCantReply() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(replyWithRole(new Message(), new Message(), ROLE)).as("replyWithRole with wrong args")
						.isEqualTo(ReturnCode.CANT_REPLY);
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenWrongArgFromMessageSentFromAnObject_thenReturnCantReply() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				receiveMessage(new Message());
				assertThat(replyWithRole(new Message(), nextMessage(), ROLE))
						.as("replyWithRole with wrong args from object message").isEqualTo(ReturnCode.CANT_REPLY);
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenNullArg_thenHandleNullPointerException() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				assertThatThrownBy(() -> replyWithRole(null, nextMessage(), ROLE))
						.as("replyWithRole(null, nextMessage(), ROLE)").isInstanceOf(NullPointerException.class);
				resume();
			}
		});

	}

	@Test
	public void givennAgent_whenNullArg_thenHandleNullPointerException() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				assertThatThrownBy(() -> replyWithRole(new Message(), null, ROLE))
						.as("replyWithRole(new Message(), null, ROLE)").isInstanceOf(NullPointerException.class);
				resume();
			}
		});
	}
}
