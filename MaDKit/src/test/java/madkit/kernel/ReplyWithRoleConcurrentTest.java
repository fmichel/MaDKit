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

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.messages.StringMessage;
import madkit.messaging.ForEverReplierAgent;
import madkit.test.agents.CGRAgent;

/**
 * The Class ReplyWithRoleConcurrentTest.
 * 
 * @version 6.0.4
 */
public class ReplyWithRoleConcurrentTest extends MadkitConcurrentTestCase {

	@Test
	public void givenAgent_whenRoleNotHandled_thenReturnRoleNotHandled() {
		runTest(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				threadAssertEquals(SUCCESS, launchAgent(new ForEverReplierAgent(StringMessage.class)));
				send(new Message(), COMMUNITY, GROUP, ROLE);
				Message waitNextMessage = waitNextMessage();
				threadAssertEquals(SUCCESS, leaveRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(ROLE_NOT_HANDLED, replyWithRole(new Message(), waitNextMessage, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenNotInGroup_thenReturnNotInGroup() {
		runTest(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				threadAssertEquals(SUCCESS, launchAgent(new ForEverReplierAgent(StringMessage.class)));
				send(new Message(), COMMUNITY, GROUP, ROLE);
				Message waitNextMessage = waitNextMessage();
				threadAssertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_IN_GROUP, replyWithRole(new Message(), waitNextMessage, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenInvalidAgentAddress_thenReturnInvalidAgentAddress() {
		runTest(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				ForEverReplierAgent target;
				threadAssertEquals(SUCCESS, launchAgent(target = new ForEverReplierAgent(StringMessage.class)));
				send(new Message(), COMMUNITY, GROUP, ROLE);
				Message waitNextMessage = waitNextMessage();
				target.leaveGroup(COMMUNITY, GROUP);
				threadAssertEquals(INVALID_AGENT_ADDRESS, replyWithRole(new Message(), waitNextMessage, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenReplyWithRole_thenReturnSuccess() {
		runTest(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				threadAssertEquals(SUCCESS, launchAgent(new ForEverReplierAgent()));
				send(new Message(), COMMUNITY, GROUP, ROLE);
				threadAssertEquals(SUCCESS, replyWithRole(new Message(), waitNextMessage(), ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenWrongArg_thenReturnCantReply() {
		runTest(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				threadAssertEquals(ReturnCode.CANT_REPLY, replyWithRole(new Message(), new Message(), ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenWrongArgFromMessageSentFromAnObject_thenReturnCantReply() {
		runTest(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				receiveMessage(new Message());
				threadAssertEquals(ReturnCode.CANT_REPLY, replyWithRole(new Message(), nextMessage(), ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenNullArg_thenHandleNullPointerException() {
		runTest(new CGRAgent() {
			@Override
			protected void onActivation() {
				try {
					threadAssertEquals(SUCCESS, replyWithRole(null, nextMessage(), ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
					resume();
				}
			}
		});

	}

	@Test
	public void givennAgent_whenNullArg_thenHandleNullPointerException() {
		runTest(new CGRAgent() {
			@Override
			protected void onActivation() {
				try {
					threadAssertEquals(SUCCESS, replyWithRole(new Message(), null, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
					;
					resume();
				}
			}
		});
	}
}
