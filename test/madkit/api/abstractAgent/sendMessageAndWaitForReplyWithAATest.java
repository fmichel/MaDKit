/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.INVALID_AA;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.logging.Level;

import madkit.agr.Organization;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Message;
import madkit.message.StringMessage;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.8
 * @version 0.9
 * 
 */
@SuppressWarnings("all")
public class sendMessageAndWaitForReplyWithAATest extends JunitMadKit {

	final Agent target = new Agent() {
		AgentAddress aa;

		protected void activate() {
			assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
			aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
			assertNotNull(aa);
			assertEquals(SUCCESS, sendMessage(aa, new Message()));
			assertEquals(SUCCESS, sendMessage(aa, new Message()));
		}

		protected void live() {
			waitNextMessage();// waiting the start signal
			sendReply(waitNextMessage(), new StringMessage("reply"));
			assertEquals(SUCCESS, sendMessage(aa, new Message()));
			assertEquals(SUCCESS, sendMessage(aa, new Message()));
			sendReply(waitNextMessage(), new StringMessage("reply2"));
		}
	};

	// sends the same message as reply
	final Agent target3 = new Agent() {
		protected void activate() {
			assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		}

		protected void live() {
			Message m = waitNextMessage();
			sendReply(m, m);
			waitNextMessage();// do not die !
		}
	};

	final Agent target2 = new Agent() {
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
		}

		protected void live() {
			sendReply(waitNextMessage(), new StringMessage("reply"));
			sendReply(waitNextMessage(), new StringMessage("reply2"));
		}
	};

	@Test
	public void replyWithSameMessage() {
		launchTest(new Agent() {
			protected void activate() {
				setLogLevel(Level.ALL);
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				// assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				assertEquals(SUCCESS, launchAgent(target3));

				assertEquals(SUCCESS, sendMessage(COMMUNITY, GROUP, ROLE, new Message()));
				assertNotNull(waitNextMessage(100));
			}
		});
	}

	@Test
	public void returnSuccess() {
		launchTest(new Agent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));

				assertFalse(this.isMessageBoxEmpty());
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertNotNull(aa);

				// time out but gives the start signal
				assertNull(sendMessageAndWaitForReply(aa, new Message(), 100));

				// Without role
				Message m = sendMessageAndWaitForReply(aa, new Message());
				assertNotNull(m);
				assertEquals("reply", ((StringMessage) m).getContent());
				assertEquals(ROLE, m.getReceiver().getRole());

				assertFalse(this.isMessageBoxEmpty());

				// With role
				m = sendMessageAndWaitForReply(aa, new Message());
				assertNotNull(m);
				assertEquals("reply2", ((StringMessage) m).getContent());
				assertEquals(ROLE, m.getReceiver().getRole());

				assertNotNull(nextMessage());
				assertNotNull(nextMessage());
				assertNotNull(nextMessage());
				assertNotNull(nextMessage());
				assertNull(nextMessage());
				pause(100);
				assertEquals(INVALID_AA, sendMessage(aa, new Message()));// the
																							// target
																							// has
																							// gone:
																							// AgentAddress
																							// no
																							// longer
																							// valid
				assertNull(sendMessageAndWaitForReply(aa, new Message()));// the
																								// target
																								// has
																								// gone:
																								// AgentAddress
																								// no
																								// longer
																								// valid
			}
		});
	}

	@Test
	public void returnSuccessOnCandidateRole() {
		launchTest(new Agent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target2));

				// Without role
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE);
				assertNotNull(aa);
				Message m = sendMessageAndWaitForReply(aa, new Message());
				assertNotNull(m);
				assertEquals("reply", ((StringMessage) m).getContent());
				assertEquals(Organization.GROUP_CANDIDATE_ROLE, m.getReceiver().getRole());
				assertEquals(Organization.GROUP_MANAGER_ROLE, m.getSender().getRole());

				// With role
				m = sendMessageWithRoleAndWaitForReply(aa, new Message(), Organization.GROUP_CANDIDATE_ROLE);
				assertNotNull(m);
				assertEquals("reply2", ((StringMessage) m).getContent());
				assertEquals(Organization.GROUP_CANDIDATE_ROLE, m.getReceiver().getRole());
				assertEquals(Organization.GROUP_MANAGER_ROLE, m.getSender().getRole());
			}
		});
	}

	@Test
	public void returnInvalidAA() {
		launchTest(new Agent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
				assertNull(sendMessageAndWaitForReply(aa, new Message()));// INVALID_AA
																								// warning
				assertEquals(INVALID_AA, sendMessage(aa, new Message()));

				// With role
				assertNull(sendMessageWithRoleAndWaitForReply(aa, new Message(), ROLE));// INVALID_AA
																												// warning
			}
		});
	}

	@Test
	public void returnBadCGR() {
		launchTest(new Agent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));

				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertNull(sendMessageWithRoleAndWaitForReply(aa, new Message(), aa()));// not
																												// role
																												// warning
				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertNull(sendMessageAndWaitForReply(aa, new Message()));// not in
																								// group
																								// warning

			}
		});
	}

	@Test
	public void nullArgs() {
		launchTest(new Agent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				try {
					sendMessageAndWaitForReply(null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					sendMessageAndWaitForReply(aa, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					sendMessageAndWaitForReply(null, new Message());
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
