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
package com.distrimind.madkit.api.abstractAgent;

import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.INVALID_AGENT_ADDRESS;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.logging.Level;

import org.junit.Test;

import com.distrimind.madkit.agr.Organization;
import com.distrimind.madkit.kernel.Agent;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.message.StringMessage;
import com.distrimind.madkit.testing.util.agent.ForEverReplierAgent;
import com.distrimind.madkit.testing.util.agent.NormalAgent;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.8
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */
public class sendMessageAndWaitForReplyWithAATest extends JunitMadkit {

	final Agent target = new Agent() {
		AgentAddress aa;

		@Override
		protected void activate() {
			assertEquals(SUCCESS, requestRole(GROUP, ROLE));
			aa = getAgentWithRole(GROUP, ROLE);
			assertNotNull(aa);
			assertEquals(SUCCESS, sendMessage(aa, new Message()));
			assertEquals(SUCCESS, sendMessage(aa, new Message()));
		}

		@Override
		protected void liveCycle() throws InterruptedException {
			waitNextMessage(2000);// waiting the start signal
			Message m = waitNextMessage(2000);
			if (m != null) {
				sendReply(m, new StringMessage("reply"));
				assertEquals(SUCCESS, sendMessage(aa, new Message()));
				assertEquals(SUCCESS, sendMessage(aa, new Message()));
				m = waitNextMessage(2000);
				if (m != null)
					sendReply(m, new StringMessage("reply2"));
				else
					this.killAgent(this);
			}

			this.killAgent(this);
		}

	};

	// sends the same message as reply
	final Agent target3 = new Agent() {
		@Override
		protected void activate() {
			assertEquals(SUCCESS, requestRole(GROUP, ROLE));
		}

		@Override
		protected void liveCycle() throws InterruptedException {
			Message m = waitNextMessage(2000);
			if (m != null) {
				sendReply(m, m);
				waitNextMessage(2000);// do not die !
			}
			this.killAgent(this);
		}
	};

	final Agent target2 = new Agent() {
		private int cycles = 10;

		@Override
		protected void activate() {
			assertEquals(SUCCESS, createGroup(GROUP));
		}

		@Override
		protected void liveCycle() throws InterruptedException {
			Message m = waitNextMessage(2000);
			if (m != null) {
				sendReply(m, new StringMessage("reply"));
				m = waitNextMessage(2000);
				if (m != null)
					sendReply(m, new StringMessage("reply2"));
				else
					this.killAgent(this);
			} else
				this.killAgent(this);
			if (cycles-- == 0)
				this.killAgent(this);

		}
	};

	@Test
	public void replyWithSameMessage() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				setLogLevel(Level.ALL);
				assertEquals(SUCCESS, createGroup(GROUP));
				// assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				assertEquals(SUCCESS, launchAgent(target3));

				assertEquals(SUCCESS, sendMessage(GROUP, ROLE, new Message()));
				assertNotNull(waitNextMessage(100));

			}
		});
	}

	@Test
	public void sendReplyAndWaitForReply() {
		addMadkitArgs("--kernelLogLevel", "ALL");
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				super.activate();
				assertEquals(SUCCESS, launchAgent(new ForEverReplierAgent()));
				Message m = sendMessageAndWaitForReply(GROUP, ROLE, new Message());
				m = sendReplyAndWaitForReply(m, new Message());
				assertNotNull(m);
				m = sendReplyAndWaitForReply(m, new Message());
				assertNotNull(m);
			}
		});
	}

	@Test
	public void sendReplyOnWaitNextMessage() {
		addMadkitArgs("--kernelLogLevel", "ALL");
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				super.activate();
				assertEquals(SUCCESS, launchAgent(new ForEverReplierAgent()));
				sendReply(waitNextMessage(), new Message());
				sendReply(waitNextMessage(), new Message());
				Message m = waitNextMessage();
				assertEquals(getAgentAddressIn(GROUP, ROLE), m.getReceiver());
			}
		});
	}

	@Test
	public void returnSuccess() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));

				assertFalse(this.isMessageBoxEmpty());
				AgentAddress aa = getAgentWithRole(GROUP, ROLE);
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
				assertEquals(INVALID_AGENT_ADDRESS, sendMessage(aa, new Message()));// the
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
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, launchAgent(target2));

				// Without role
				AgentAddress aa = getAgentWithRole(GROUP, Organization.GROUP_MANAGER_ROLE);
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
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));
				AgentAddress aa = getAgentWithRole(GROUP, ROLE);
				assertEquals(SUCCESS, target.leaveRole(GROUP, ROLE));
				assertNull(sendMessageAndWaitForReply(aa, new Message()));// INVALID_AGENT_ADDRESS
																			// warning
				assertEquals(INVALID_AGENT_ADDRESS, sendMessage(aa, new Message()));

				// With role
				assertNull(sendMessageWithRoleAndWaitForReply(aa, new Message(), ROLE));// INVALID_AGENT_ADDRESS
																						// warning
			}
		});
	}

	@Test
	public void returnBadCGR() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));

				AgentAddress aa = getAgentWithRole(GROUP, ROLE);
				assertNull(sendMessageWithRoleAndWaitForReply(aa, new Message(), aa()));// not
																						// role
																						// warning
				assertEquals(SUCCESS, leaveGroup(GROUP));
				assertNull(sendMessageAndWaitForReply(aa, new Message()));// not in
																			// group
																			// warning

			}
		});
	}

	@Test
	public void nullArgs() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));
				AgentAddress aa = getAgentWithRole(GROUP, ROLE);
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
