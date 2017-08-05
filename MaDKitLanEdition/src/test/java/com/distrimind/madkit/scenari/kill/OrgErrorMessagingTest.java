/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.scenari.kill;

import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.INVALID_AGENT_ADDRESS;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.NOT_ROLE;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.NO_RECIPIENT_FOUND;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.ROLE_ALREADY_HANDLED;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.distrimind.madkit.agr.Organization;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.testing.util.agent.NormalAgent;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.10
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 * 
 */

public class OrgErrorMessagingTest extends JunitMadkit {

	final static String OTHER = "other";
	final static String UNKNOWN = "unknown";

	@Test
	public void sendMessageTesting() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));

				AbstractAgent testAgent = launchAgent(AbstractAgent.class.getName());
				testAgent.requestRole(new Group(C, OTHER), OTHER);

				assertEquals(NOT_COMMUNITY, sendMessage(new Group("unknown", G), ROLE, new Message()));
				assertEquals(NOT_GROUP, sendMessage(new Group(C, "unknown"), ROLE, new Message()));
				assertEquals(NOT_ROLE, sendMessage(GROUP, "unknown", new Message()));
				assertEquals(NOT_COMMUNITY, sendMessageWithRole(new Group("unknown", G), ROLE, new Message(), "any"));
				assertEquals(NOT_GROUP, sendMessageWithRole(new Group(C, "unknown"), ROLE, new Message(), "any"));
				assertEquals(NOT_ROLE, sendMessageWithRole(GROUP, "unknown", new Message(), "any"));

				// try in the group OTHER
				assertEquals(NOT_ROLE, sendMessage(new Group(C, OTHER), UNKNOWN, new Message()));
				assertEquals(NOT_IN_GROUP, sendMessage(new Group(C, OTHER), OTHER, new Message()));
				assertEquals(NOT_IN_GROUP, sendMessageWithRole(new Group(C, OTHER), OTHER, new Message(), OTHER));

				// the candidate role should be used to send message to the manager
				assertEquals(NOT_IN_GROUP, sendMessageWithRole(new Group(C, OTHER), OTHER, new Message(),
						Organization.GROUP_CANDIDATE_ROLE));
				assertEquals(NOT_IN_GROUP, sendMessageWithRole(new Group(C, OTHER), Organization.GROUP_MANAGER_ROLE,
						new Message(), OTHER));
				assertEquals(SUCCESS, sendMessageWithRole(new Group(C, OTHER), Organization.GROUP_MANAGER_ROLE,
						new Message(), Organization.GROUP_CANDIDATE_ROLE));
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
				assertEquals(NO_RECIPIENT_FOUND, sendMessageWithRole(GROUP, ROLE, new Message(), "any"));

				// this agent is now not alone
				testAgent.requestRole(GROUP, ROLE);

				// this agent has not this role
				assertEquals(ROLE_NOT_HANDLED, sendMessageWithRole(GROUP, ROLE, new Message(), "any"));

				// this agent has this role
				assertEquals(SUCCESS, sendMessageWithRole(GROUP, ROLE, new Message(), ROLE));
				// trash fake agent mailbox
				assertNotNull(testAgent.nextMessage());

				// now take some roles to test some other properties
				assertEquals(SUCCESS, requestRole(GROUP, "r1"));
				assertEquals(SUCCESS, requestRole(GROUP, "r2"));
				assertEquals(SUCCESS, requestRole(GROUP, "r3"));

				// if I send a message without saying about the role : the receiver
				// role is selected if I have it
				assertEquals(SUCCESS, sendMessage(GROUP, ROLE, new Message()));
				// check reception
				Message m2 = testAgent.nextMessage();
				assertNotNull(m2);
				assertNull(testAgent.nextMessage());
				assertEquals(ROLE, m2.getSender().getRole());
				assertEquals(ROLE, m2.getReceiver().getRole());

				// if I send a message with saying the role
				assertEquals(SUCCESS, sendMessageWithRole(GROUP, ROLE, new Message(), "r2"));
				// check reception
				Message m3 = testAgent.nextMessage();
				assertNotNull(m3);
				assertNull(testAgent.nextMessage());
				assertEquals("r2", m3.getSender().getRole());
				assertEquals(ROLE, m3.getReceiver().getRole());

				assertEquals(SUCCESS, leaveGroup(GROUP));

				// I am not in this group anymore
				assertEquals(NOT_IN_GROUP, sendMessageWithRole(GROUP, ROLE, new Message(), "any"));
				assertEquals(NOT_IN_GROUP,
						sendMessageWithRole(GROUP, ROLE, new Message(), Organization.GROUP_CANDIDATE_ROLE));
				assertEquals(NOT_IN_GROUP, sendMessage(GROUP, ROLE, new Message()));
				assertEquals(NOT_IN_GROUP, sendMessage(GROUP, ROLE, new Message()));
				// TODO what if no manager left
				// TODO rehabilitate this test
				/*
				 * assertEquals( SUCCESS, sendMessageWithRole(GROUP,
				 * Organization.GROUP_MANAGER_ROLE, new Message(),
				 * Organization.GROUP_CANDIDATE_ROLE));
				 */

				// this agent has leaved the group so m2.getSender() is invalid
				assertEquals(INVALID_AGENT_ADDRESS, testAgent.sendMessage(m2.getSender(), new Message()));

				m3 = testAgent.nextMessage();
				assertNull(m3);
				/*
				 * assertNull(testAgent.nextMessage());
				 * 
				 * // testAgent can reply as group manager assertEquals(SUCCESS,
				 * testAgent.sendMessage(m3.getSender(), new Message()));
				 * 
				 * // empty mailbox m3 = nextMessage(); assertNotNull(m3);
				 * assertEquals(Organization.GROUP_MANAGER_ROLE, m3.getSender().getRole());
				 * assertEquals(Organization.GROUP_CANDIDATE_ROLE, m3.getReceiver().getRole());
				 */

				assertNull(nextMessage());
				assertNull(testAgent.nextMessage());

				// testThreadedAgent(new ErrorAgent());

				// cleaning up
				assertEquals(SUCCESS, testAgent.leaveGroup(GROUP));
				assertEquals(SUCCESS, testAgent.leaveGroup(new Group(C, OTHER)));
				assertFalse(isCommunity(C));
				assertFalse(isCreatedGroup(GROUP));
				assertNull(testAgent.nextMessage());
				assertNull(nextMessage());
			}
		});
	}

	@Test
	public void testingSendMessage() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));

				assertEquals(NOT_COMMUNITY, sendMessage(new Group("unknown", G), ROLE, new Message()));
				assertEquals(NOT_GROUP, sendMessage(new Group(C, "unknown"), ROLE, new Message()));
				assertEquals(NOT_ROLE, sendMessage(GROUP, "unknown", new Message()));

				assertEquals(NOT_COMMUNITY, sendMessageWithRole(new Group("unknown", G), ROLE, new Message(), "any"));
				assertEquals(NOT_GROUP, sendMessageWithRole(new Group(C, "unknown"), ROLE, new Message(), "any"));
				assertEquals(NOT_ROLE, sendMessageWithRole(GROUP, "unknown", new Message(), "any"));

				assertNull(sendMessageAndWaitForReply(new Group("unknown", G), ROLE, new Message()));
				assertNull(sendMessageAndWaitForReply(new Group(C, "unknown"), ROLE, new Message()));
				assertNull(sendMessageAndWaitForReply(GROUP, "unknown", new Message()));

				assertNull(sendMessageWithRoleAndWaitForReply(new Group("unknown", G), ROLE, new Message(), "any"));
				assertNull(sendMessageWithRoleAndWaitForReply(new Group(C, "unknown"), ROLE, new Message(), "any"));
				assertNull(sendMessageWithRoleAndWaitForReply(GROUP, "unknown", new Message(), "any"));

				AbstractAgent testAgent = launchAgent(AbstractAgent.class.getName());
				testAgent.requestRole(new Group(C, OTHER), OTHER);

				assertEquals(ROLE_ALREADY_HANDLED, requestRole(GROUP, ROLE));
				// try in the group OTHER
				assertEquals(NOT_ROLE, sendMessage(new Group(C, OTHER), UNKNOWN, new Message()));
				assertEquals(NOT_IN_GROUP, sendMessage(new Group(C, OTHER), OTHER, new Message()));
				assertEquals(NOT_IN_GROUP, sendMessageWithRole(new Group(C, OTHER), OTHER, new Message(), OTHER));

				// the candidate role should be used to send message to the manager
				assertEquals(NOT_IN_GROUP, sendMessageWithRole(new Group(C, OTHER), OTHER, new Message(),
						Organization.GROUP_CANDIDATE_ROLE));
				assertEquals(NOT_IN_GROUP, sendMessageWithRole(new Group(C, OTHER), Organization.GROUP_MANAGER_ROLE,
						new Message(), OTHER));
				assertEquals(SUCCESS, sendMessageWithRole(new Group(C, OTHER), Organization.GROUP_MANAGER_ROLE,
						new Message(), Organization.GROUP_CANDIDATE_ROLE));
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
				assertEquals(NO_RECIPIENT_FOUND, sendMessageWithRole(GROUP, ROLE, new Message(), "any"));

				// this agent is now not alone
				testAgent.requestRole(GROUP, ROLE);

				// this agent has not this role
				assertEquals(ROLE_NOT_HANDLED, sendMessageWithRole(GROUP, ROLE, new Message(), "any"));

				// this agent has this role
				assertEquals(SUCCESS, sendMessageWithRole(GROUP, ROLE, new Message(), ROLE));
				// trash fake agent mailbox
				assertNotNull(testAgent.nextMessage());

				// now take some roles to test some other properties
				assertEquals(SUCCESS, requestRole(GROUP, "r1"));
				assertEquals(SUCCESS, requestRole(GROUP, "r2"));
				assertEquals(SUCCESS, requestRole(GROUP, "r3"));

				// if I send a message without saying about the role : the receiver
				// role is selected if I have it
				assertEquals(SUCCESS, sendMessage(GROUP, ROLE, new Message()));
				// check reception
				Message m2 = testAgent.nextMessage();
				assertNotNull(m2);
				assertNull(testAgent.nextMessage());
				assertEquals(ROLE, m2.getSender().getRole());
				assertEquals(ROLE, m2.getReceiver().getRole());

				// if I send a message with saying the role
				assertEquals(SUCCESS, sendMessageWithRole(GROUP, ROLE, new Message(), "r2"));
				// check reception
				Message m3 = testAgent.nextMessage();
				assertNotNull(m3);
				assertNull(testAgent.nextMessage());
				assertEquals("r2", m3.getSender().getRole());
				assertEquals(ROLE, m3.getReceiver().getRole());

				assertEquals(SUCCESS, leaveGroup(GROUP));
			}
		});
	}
}