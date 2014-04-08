/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.scenari.kill;

import static madkit.kernel.AbstractAgent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_ROLE;
import static madkit.kernel.AbstractAgent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_ALREADY_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import madkit.agr.Organization;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Message;
import madkit.testing.util.agent.NormalAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */

public class OrgErrorMessagingTest extends JunitMadkit {

	final static String OTHER = "other";
	final static String UNKNOWN = "unknown";

	@Test
	public void sendMessageTesting() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, false, null));

				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));

				AbstractAgent testAgent = launchAgent(AbstractAgent.class.getName());
				testAgent.createGroup(COMMUNITY, OTHER);
				testAgent.requestRole(COMMUNITY, OTHER, OTHER);

				assertEquals(NOT_COMMUNITY, sendMessage("unknown", GROUP, ROLE, new Message()));
				assertEquals(NOT_GROUP, sendMessage(COMMUNITY, "unknown", ROLE, new Message()));
				assertEquals(NOT_ROLE, sendMessage(COMMUNITY, GROUP, "unknown", new Message()));
				assertEquals(NOT_COMMUNITY, sendMessageWithRole("unknown", GROUP, ROLE, new Message(), "any"));
				assertEquals(NOT_GROUP, sendMessageWithRole(COMMUNITY, "unknown", ROLE, new Message(), "any"));
				assertEquals(NOT_ROLE, sendMessageWithRole(COMMUNITY, GROUP, "unknown", new Message(), "any"));

				// try in the group OTHER
				assertEquals(NOT_ROLE, sendMessage(COMMUNITY, OTHER, UNKNOWN, new Message()));
				assertEquals(NOT_IN_GROUP, sendMessage(COMMUNITY, OTHER, OTHER, new Message()));
				assertEquals(NOT_IN_GROUP, sendMessageWithRole(COMMUNITY, OTHER, OTHER, new Message(), OTHER));

				// the candidate role should be used to send message to the manager
				assertEquals(NOT_IN_GROUP,
						sendMessageWithRole(COMMUNITY, OTHER, OTHER, new Message(), Organization.GROUP_CANDIDATE_ROLE));
				assertEquals(NOT_IN_GROUP,
						sendMessageWithRole(COMMUNITY, OTHER, Organization.GROUP_MANAGER_ROLE, new Message(), OTHER));
				assertEquals(
						SUCCESS,
						sendMessageWithRole(COMMUNITY, OTHER, Organization.GROUP_MANAGER_ROLE, new Message(),
								Organization.GROUP_CANDIDATE_ROLE));
				// check reception
				Message m = testAgent.nextMessage();
				assertNotNull(m);
				assertNull(testAgent.nextMessage());
				assertEquals(Organization.GROUP_CANDIDATE_ROLE, m.getSender().getRole());
				assertEquals(Organization.GROUP_MANAGER_ROLE, m.getReceiver().getRole());
				// fake agent is replying
				assertEquals(SUCCESS, testAgent.sendMessage(m.getSender(), new Message()));

				m = nextMessage();
				assertNotNull(m);
				assertEquals(Organization.GROUP_MANAGER_ROLE, m.getSender().getRole());
				assertEquals(Organization.GROUP_CANDIDATE_ROLE, m.getReceiver().getRole());
				assertEquals(SUCCESS, sendMessage(m.getSender(), new Message()));

				// trash fake agent mailbox
				assertNotNull(testAgent.nextMessage());

				// this agent is the only one there
				assertEquals(NO_RECIPIENT_FOUND, sendMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), "any"));

				// this agent is now not alone
				testAgent.requestRole(COMMUNITY, GROUP, ROLE);

				// this agent has not this role
				assertEquals(ROLE_NOT_HANDLED, sendMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), "any"));

				// this agent has this role
				assertEquals(SUCCESS, sendMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), ROLE));
				// trash fake agent mailbox
				assertNotNull(testAgent.nextMessage());

				// now take some roles to test some other properties
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, "r1"));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, "r2"));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, "r3"));

				// if I send a message without saying about the role : the receiver
				// role is selected if I have it
				assertEquals(SUCCESS, sendMessage(COMMUNITY, GROUP, ROLE, new Message()));
				// check reception
				Message m2 = testAgent.nextMessage();
				assertNotNull(m2);
				assertNull(testAgent.nextMessage());
				assertEquals(ROLE, m2.getSender().getRole());
				assertEquals(ROLE, m2.getReceiver().getRole());

				// if I send a message with saying the role
				assertEquals(SUCCESS, sendMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), "r2"));
				// check reception
				Message m3 = testAgent.nextMessage();
				assertNotNull(m3);
				assertNull(testAgent.nextMessage());
				assertEquals("r2", m3.getSender().getRole());
				assertEquals(ROLE, m3.getReceiver().getRole());

				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));

				// I am not in this group anymore
				assertEquals(NOT_IN_GROUP, sendMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), "any"));
				assertEquals(NOT_IN_GROUP,
						sendMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), Organization.GROUP_CANDIDATE_ROLE));
				assertEquals(NOT_IN_GROUP, sendMessage(COMMUNITY, GROUP, ROLE, new Message()));
				assertEquals(NOT_IN_GROUP, sendMessage(COMMUNITY, GROUP, ROLE, new Message()));
				// TODO what if no manager left
				assertEquals(
						SUCCESS,
						sendMessageWithRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE, new Message(),
								Organization.GROUP_CANDIDATE_ROLE));

				// this agent has leaved the group so m2.getSender() is invalid
				assertEquals(INVALID_AGENT_ADDRESS, testAgent.sendMessage(m2.getSender(), new Message()));

				m3 = testAgent.nextMessage();
				assertNotNull(m3);
				assertNull(testAgent.nextMessage());

				// testAgent can reply as group manager
				assertEquals(SUCCESS, testAgent.sendMessage(m3.getSender(), new Message()));

				// empty mailbox
				m3 = nextMessage();
				assertNotNull(m3);
				assertEquals(Organization.GROUP_MANAGER_ROLE, m3.getSender().getRole());
				assertEquals(Organization.GROUP_CANDIDATE_ROLE, m3.getReceiver().getRole());

				assertNull(nextMessage());
				assertNull(testAgent.nextMessage());

				// testThreadedAgent(new ErrorAgent());

				// cleaning up
				assertEquals(SUCCESS, testAgent.leaveGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, testAgent.leaveGroup(COMMUNITY, OTHER));
				assertFalse(isCommunity(COMMUNITY));
				assertFalse(isGroup(COMMUNITY, GROUP));
				assertNull(testAgent.nextMessage());
				assertNull(nextMessage());
			}
		});
	}

	@Test
	public void testingSendMessage() {
		launchTest(new NormalAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, false, null));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));

				assertEquals(NOT_COMMUNITY, sendMessage("unknown", GROUP, ROLE, new Message()));
				assertEquals(NOT_GROUP, sendMessage(COMMUNITY, "unknown", ROLE, new Message()));
				assertEquals(NOT_ROLE, sendMessage(COMMUNITY, GROUP, "unknown", new Message()));

				assertEquals(NOT_COMMUNITY, sendMessageWithRole("unknown", GROUP, ROLE, new Message(), "any"));
				assertEquals(NOT_GROUP, sendMessageWithRole(COMMUNITY, "unknown", ROLE, new Message(), "any"));
				assertEquals(NOT_ROLE, sendMessageWithRole(COMMUNITY, GROUP, "unknown", new Message(), "any"));

				assertNull(sendMessageAndWaitForReply("unknown", GROUP, ROLE, new Message()));
				assertNull(sendMessageAndWaitForReply(COMMUNITY, "unknown", ROLE, new Message()));
				assertNull(sendMessageAndWaitForReply(COMMUNITY, GROUP, "unknown", new Message()));

				assertNull(sendMessageWithRoleAndWaitForReply("unknown", GROUP, ROLE, new Message(), "any"));
				assertNull(sendMessageWithRoleAndWaitForReply(COMMUNITY, "unknown", ROLE, new Message(), "any"));
				assertNull(sendMessageWithRoleAndWaitForReply(COMMUNITY, GROUP, "unknown", new Message(), "any"));

				AbstractAgent testAgent = launchAgent(AbstractAgent.class.getName());
				testAgent.createGroup(COMMUNITY, OTHER);
				testAgent.requestRole(COMMUNITY, OTHER, OTHER);

				assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP, ROLE));
				// try in the group OTHER
				assertEquals(NOT_ROLE, sendMessage(COMMUNITY, OTHER, UNKNOWN, new Message()));
				assertEquals(NOT_IN_GROUP, sendMessage(COMMUNITY, OTHER, OTHER, new Message()));
				assertEquals(NOT_IN_GROUP, sendMessageWithRole(COMMUNITY, OTHER, OTHER, new Message(), OTHER));

				// the candidate role should be used to send message to the manager
				assertEquals(NOT_IN_GROUP,
						sendMessageWithRole(COMMUNITY, OTHER, OTHER, new Message(), Organization.GROUP_CANDIDATE_ROLE));
				assertEquals(NOT_IN_GROUP,
						sendMessageWithRole(COMMUNITY, OTHER, Organization.GROUP_MANAGER_ROLE, new Message(), OTHER));
				assertEquals(
						SUCCESS,
						sendMessageWithRole(COMMUNITY, OTHER, Organization.GROUP_MANAGER_ROLE, new Message(),
								Organization.GROUP_CANDIDATE_ROLE));
				// check reception
				Message m = testAgent.nextMessage();
				assertNotNull(m);
				assertNull(testAgent.nextMessage());
				assertEquals(Organization.GROUP_CANDIDATE_ROLE, m.getSender().getRole());
				assertEquals(Organization.GROUP_MANAGER_ROLE, m.getReceiver().getRole());
				// fake agent is replying
				assertEquals(SUCCESS, testAgent.sendMessage(m.getSender(), new Message()));

				m = nextMessage();
				assertNotNull(m);
				assertEquals(Organization.GROUP_MANAGER_ROLE, m.getSender().getRole());
				assertEquals(Organization.GROUP_CANDIDATE_ROLE, m.getReceiver().getRole());
				assertEquals(SUCCESS, sendMessage(m.getSender(), new Message()));

				// trash fake agent mailbox
				assertNotNull(testAgent.nextMessage());

				// this agent is the only one there
				assertEquals(NO_RECIPIENT_FOUND, sendMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), "any"));

				// this agent is now not alone
				testAgent.requestRole(COMMUNITY, GROUP, ROLE);

				// this agent has not this role
				assertEquals(ROLE_NOT_HANDLED, sendMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), "any"));

				// this agent has this role
				assertEquals(SUCCESS, sendMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), ROLE));
				// trash fake agent mailbox
				assertNotNull(testAgent.nextMessage());

				// now take some roles to test some other properties
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, "r1"));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, "r2"));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, "r3"));

				// if I send a message without saying about the role : the receiver
				// role is selected if I have it
				assertEquals(SUCCESS, sendMessage(COMMUNITY, GROUP, ROLE, new Message()));
				// check reception
				Message m2 = testAgent.nextMessage();
				assertNotNull(m2);
				assertNull(testAgent.nextMessage());
				assertEquals(ROLE, m2.getSender().getRole());
				assertEquals(ROLE, m2.getReceiver().getRole());

				// if I send a message with saying the role
				assertEquals(SUCCESS, sendMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), "r2"));
				// check reception
				Message m3 = testAgent.nextMessage();
				assertNotNull(m3);
				assertNull(testAgent.nextMessage());
				assertEquals("r2", m3.getSender().getRole());
				assertEquals(ROLE, m3.getReceiver().getRole());

				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
			}
		});
	}
}