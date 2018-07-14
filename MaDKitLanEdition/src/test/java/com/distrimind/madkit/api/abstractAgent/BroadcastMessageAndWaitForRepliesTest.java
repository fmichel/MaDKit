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

import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import org.junit.Test;

import com.distrimind.madkit.kernel.Agent;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.message.StringMessage;
import com.distrimind.madkit.testing.util.agent.NormalAgent;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class BroadcastMessageAndWaitForRepliesTest extends JunitMadkit {

	final Agent target = new NormalAgent() {
		int cycles = 10;

		@Override
		protected void activate() {
			assertEquals(SUCCESS, requestRole(GROUP, ROLE));
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

	// sends the same message as reply
	final Agent target3 = new NormalAgent() {
		private int cycles = 10;

		@Override
		protected void activate() {
			assertEquals(SUCCESS, requestRole(GROUP, ROLE));
		}

		@Override
		protected void liveCycle() throws InterruptedException {
			Message m = waitNextMessage(2000);
			if (m != null) {
				sendReply(m, m);
				if (waitNextMessage(2000) == null)
					this.killAgent(this);
			} else
				this.killAgent(this);
			if (cycles-- == 0)
				this.killAgent(this);
		}
	};

	final Agent target2 = new NormalAgent() {
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
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target3));
				assertEquals(1,
						broadcastMessageWithRoleAndWaitForReplies(GROUP, ROLE, new Message(), null, 100)
								.getReplies().size());
			}
		});
	}

	@Test
	public void returnAllSuccess() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent()));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent()));
				assertEquals(2,
						broadcastMessageWithRoleAndWaitForReplies(GROUP, ROLE, new Message(), null, 100)
								.getReplies().size());
				assertEquals(2,
						broadcastMessageWithRoleAndWaitForReplies(GROUP, ROLE, new Message(), null, 100)
								.getReplies().size());
			}
		});
	}

	@Test
	public void returnAllSuccessWithSomeEmptyReplies() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent()));
				assertEquals(SUCCESS, launchAgent(new sendEmptyReplyInLiveAgent()));
				assertEquals(SUCCESS, launchAgent(new sendEmptyReplyInLiveAgent()));
				assertEquals(1,
						broadcastMessageWithRoleAndWaitForReplies(GROUP, ROLE, new Message(), null, 100)
								.getReplies().size());
				assertEquals(1,
						broadcastMessageWithRoleAndWaitForReplies(GROUP, ROLE, new Message(), null, 100)
								.getReplies().size());
			}
		});
	}

	@Test
	public void returnAllSuccessWithAllEmptyReplies() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(new sendEmptyReplyInLiveAgent()));
				assertEquals(SUCCESS, launchAgent(new sendEmptyReplyInLiveAgent()));
				assertEquals(0,
						broadcastMessageWithRoleAndWaitForReplies(GROUP, ROLE, new Message(), null, 100)
								.getReplies().size());
				assertEquals(0,
						broadcastMessageWithRoleAndWaitForReplies(GROUP, ROLE, new Message(), null, 100)
								.getReplies().size());
			}
		});
	}

	@Test
	public void returnNullOnTimeout() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent()));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent()));
				assertEquals(0,
						broadcastMessageWithRoleAndWaitForReplies(GROUP, ROLE, new Message(), null, 0)
								.getReplies().size());
				assertEquals(0,
						broadcastMessageWithRoleAndWaitForReplies(GROUP, ROLE, new Message(), null, -1)
								.getReplies().size());
			}
		});
	}

	@Test
	public void returnOneSuccess() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent()));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent(1000)));
				assertEquals(1,
						broadcastMessageWithRoleAndWaitForReplies(GROUP, ROLE, new Message(), null, 100)
								.getReplies().size());
				assertEquals(1,
						broadcastMessageWithRoleAndWaitForReplies(GROUP, ROLE, new Message(), null, 100)
								.getReplies().size());
			}
		});
	}

	// @Test
	// public void returnSuccessOnCandidateRole(){
	// launchTest(new NormalAgent(){
	// protected void activate() {
	// assertEquals(SUCCESS,launchAgent(target2));
	//
	// //Without role
	// AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP,
	// Organization.GROUP_MANAGER_ROLE);
	// assertNotNull(aa);
	// Message m = sendMessageAndWaitForReply(aa, new Message());
	// assertNotNull(m);
	// assertEquals("reply", ((StringMessage)m).getContent());
	// assertEquals(Organization.GROUP_CANDIDATE_ROLE,
	// m.getReceiver().getRole());
	// assertEquals(Organization.GROUP_MANAGER_ROLE, m.getSender().getRole());
	//
	// //With role
	// m = sendMessageWithRoleAndWaitForReply(aa, new Message(),
	// Organization.GROUP_CANDIDATE_ROLE);
	// assertNotNull(m);
	// assertEquals("reply2", ((StringMessage)m).getContent());
	// assertEquals(Organization.GROUP_CANDIDATE_ROLE,
	// m.getReceiver().getRole());
	// assertEquals(Organization.GROUP_MANAGER_ROLE, m.getSender().getRole());
	// }});
	// }

	@Test
	public void returnBadCGR() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));

				@SuppressWarnings("unused")
				AgentAddress aa = getAgentWithRole(GROUP, ROLE);
				assertEquals(0,
						broadcastMessageWithRoleAndWaitForReplies(GROUP, ROLE, new Message(), aa(), 100)
								.getReplies().size());// not
														// role
														// warning
				assertEquals(SUCCESS, leaveGroup(GROUP));
				assertEquals(0,
						broadcastMessageWithRoleAndWaitForReplies(GROUP, ROLE, new Message(), aa(), 100)
								.getReplies().size());// not
														// role
														// warning

			}
		});
	}

	@Test
	public void returnNotCGR() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));

				@SuppressWarnings("unused")
				AgentAddress aa = getAgentWithRole(GROUP, ROLE);
				assertEquals(0, broadcastMessageWithRoleAndWaitForReplies(new Group(aa(), G), ROLE, new Message(), null,
						100).getReplies().size());
				assertEquals(SUCCESS, leaveGroup(GROUP));
				assertEquals(0, broadcastMessageWithRoleAndWaitForReplies(new Group(C, aa()), ROLE, new Message(), null,
						100).getReplies().size());
				assertEquals(0,
						broadcastMessageWithRoleAndWaitForReplies(GROUP, aa(), new Message(), null, 100)
								.getReplies().size());
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(0,
						broadcastMessageWithRoleAndWaitForReplies(GROUP, ROLE, new Message(), aa(), 100)
								.getReplies().size());

			}
		});
	}

	@Test
	public void nullCommunity() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				broadcastMessageWithRoleAndWaitForReplies(new Group(null, aa()), aa(), new Message(), null,
						Integer.valueOf(0));
				noExceptionFailure();
			}
		}, AGENT_CRASH);
	}

	@Test
	public void nullGroup() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));
				broadcastMessageWithRoleAndWaitForReplies(null, aa(), new Message(), null, Integer.valueOf(0));
				noExceptionFailure();
			}
		}, AGENT_CRASH);
	}

	@Test
	public void nullRole() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));
				broadcastMessageWithRoleAndWaitForReplies(GROUP, null, new Message(), null, Integer.valueOf(0));
				noExceptionFailure();
			}
		}, AGENT_CRASH);
	}

	@Test
	public void nullMessage() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));
				broadcastMessageWithRoleAndWaitForReplies(GROUP, ROLE, null, null, Integer.valueOf(0));
				noExceptionFailure();
			}

		}, AGENT_CRASH);
	}

}

class sendReplyInLiveAgent extends Agent {
	private int cycles = 10;
	private final int time;

	public sendReplyInLiveAgent(int timeToReply) {
		time = timeToReply;
	}

	public sendReplyInLiveAgent() {
		time = 0;
	}

	@Override
	protected void activate() {
		assertEquals(SUCCESS, requestRole(JunitMadkit.GROUP, JunitMadkit.ROLE));
	}

	@Override
	protected void liveCycle() throws InterruptedException {
		pause(time);
		Message m = waitNextMessage(2000);
		if (m != null) {
			sendReply(m, new StringMessage("reply " + getAgentID()));
			pause(time);
			m = waitNextMessage(2000);
			if (m != null)
				sendReply(m, new StringMessage("reply " + getAgentID()));
			else
				this.killAgent(this);
		} else
			this.killAgent(this);
		if (cycles-- == 0)
			this.killAgent(this);
	}
}

class sendEmptyReplyInLiveAgent extends Agent {
	private int cycles = 10;
	private final int time;

	public sendEmptyReplyInLiveAgent(int timeToReply) {
		time = timeToReply;
	}

	public sendEmptyReplyInLiveAgent() {
		time = 0;
	}

	@Override
	protected void activate() {
		assertEquals(SUCCESS, requestRole(JunitMadkit.GROUP, JunitMadkit.ROLE));
	}

	@Override
	protected void liveCycle() throws InterruptedException {
		pause(time);
		Message m = waitNextMessage(2000);
		if (m != null) {
			sendReplyEmpty(m);
			pause(time);
			m = waitNextMessage(2000);
			if (m != null)
				sendReplyEmpty(m);
			else
				this.killAgent(this);
		} else
			this.killAgent(this);
		if (cycles-- == 0)
			this.killAgent(this);
	}
}