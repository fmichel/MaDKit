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

import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.NOT_ROLE;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.NO_RECIPIENT_FOUND;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;
import org.junit.Assert;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.Replies;
import com.distrimind.madkit.message.StringMessage;
import com.distrimind.madkit.testing.util.agent.NormalAgent;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.7
 * @since MadkitLanEdition 1.0
 * @version 0.92
 * 
 */

public class BroadcastMessageTest extends JunitMadkit {

	final AbstractAgent target = new AbstractAgent() {
		@Override
		protected void activate() {
			assertEquals(SUCCESS, createGroup(GROUP));
			assertEquals(SUCCESS, requestRole(GROUP, ROLE));
		}
	};

	final AbstractAgent target2 = new AbstractAgent() {
		@Override
		protected void activate() {
			assertEquals(SUCCESS, requestRole(GROUP, ROLE));
		}
	};

	@Test
	public void returnRepliesInOneBlockAllSuccess() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent()));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent()));

			}

			@Override
			public void liveCycle() throws InterruptedException {
				assertEquals(SUCCESS, broadcastMessage(GROUP, ROLE, new Message(), true));
				Message m = waitNextMessage(1000);
				Assert.assertNotNull(m);
				System.out.println(m);
				Assert.assertEquals(Replies.class, m.getClass());
				Assert.assertEquals(2, ((Replies) m).getReplies().size());
				this.killAgent(this);
			}
		});
	}

	@Test
	public void returnRepliesInOneBlockAllSuccessWithSomeEmptyReplies() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent()));
				assertEquals(SUCCESS, launchAgent(new sendEmptyReplyInLiveAgent()));
				assertEquals(SUCCESS, launchAgent(new sendEmptyReplyInLiveAgent()));

			}

			@Override
			public void liveCycle() throws InterruptedException {

				assertEquals(SUCCESS, broadcastMessage(GROUP, ROLE, new Message(), true));
				Message m = waitNextMessage(1000);
				Assert.assertEquals(Replies.class, m.getClass());
				Assert.assertEquals(1, ((Replies) m).getReplies().size());
				this.killAgent(this);
			}
		});
	}

	@Test
	public void returnRepliesInOneBlockAllSuccessWithAllEmptyReplies() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(new sendEmptyReplyInLiveAgent()));
				assertEquals(SUCCESS, launchAgent(new sendEmptyReplyInLiveAgent()));

			}

			@Override
			public void liveCycle() throws InterruptedException {
				assertEquals(SUCCESS, broadcastMessage(GROUP, ROLE, new Message(), true));
				Message m = waitNextMessage(1000);
				Assert.assertNotNull(m);
				Assert.assertEquals(Replies.class, m.getClass());
				Assert.assertEquals(0, ((Replies) m).getReplies().size());
				this.killAgent(this);
			}
		});
	}

	@Test
	public void returnSuccess() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));

				// Without role
				assertEquals(SUCCESS, broadcastMessage(GROUP, ROLE, new Message()));
				Message m = target.nextMessage();
				assertEquals(SUCCESS, broadcastMessage(GROUP, ROLE, new StringMessage("test")));
				assertNotNull(target.nextMessage());
				assertEquals(ROLE, m.getReceiver().getRole());

				// With role
				assertEquals(SUCCESS, broadcastMessageWithRole(GROUP, ROLE, new Message(), ROLE));
				m = target.nextMessage();
				assertNotNull(m);
				assertEquals(ROLE, m.getReceiver().getRole());

				// verifying cloning
				launchAgent(target2);
				assertEquals(SUCCESS, broadcastMessageWithRole(GROUP, ROLE, new Message(), ROLE));
				m = target.nextMessage();
				assertNotNull(m);
				Message m2 = target2.nextMessage();
				assertEquals(ROLE, m2.getReceiver().getRole());
				assertEquals(m.getConversationID(), m2.getConversationID());
				assertNotSame(m2, m);
			}
		});
	}

	@Test
	public void returnNotInGroup() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				// Without role
				assertEquals(NOT_IN_GROUP, broadcastMessage(GROUP, ROLE, new Message()));

				// With role
				assertEquals(NOT_IN_GROUP, broadcastMessageWithRole(GROUP, ROLE, new Message(), ROLE));
			}
		});
	}

	@Test
	public void returnNotCGR() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(NOT_COMMUNITY, broadcastMessage(new Group(aa(), G), ROLE, new Message()));
				assertEquals(NOT_GROUP, broadcastMessage(new Group(C, aa()), ROLE, new Message()));
				assertEquals(NOT_ROLE, broadcastMessage(GROUP, aa(), new Message()));

				// With role
				assertEquals(NOT_COMMUNITY, broadcastMessageWithRole(new Group(aa(), G), ROLE, new Message(), ROLE));
				assertEquals(NOT_GROUP, broadcastMessageWithRole(new Group(C, aa()), ROLE, new Message(), ROLE));
				assertEquals(NOT_ROLE, broadcastMessageWithRole(GROUP, aa(), new Message(), ROLE));
			}
		});
	}

	@Test
	public void returnRoleNotHandled() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(ROLE_NOT_HANDLED, broadcastMessageWithRole(GROUP, ROLE, new Message(), aa()));
			}
		});
	}

	@Test
	public void returnNoRecipientFound() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, target.leaveRole(GROUP, ROLE));
				assertEquals(NO_RECIPIENT_FOUND, broadcastMessage(GROUP, ROLE, new Message()));
				assertEquals(NO_RECIPIENT_FOUND, broadcastMessageWithRole(GROUP, ROLE, new Message(), ROLE));
			}
		});
	}

	@Test
	public void nullArgs() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				try {
					broadcastMessage(null, null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					broadcastMessage(GROUP, null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					assertEquals(SUCCESS, requestRole(GROUP, ROLE));
					broadcastMessage(GROUP, ROLE, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
			}
		});
	}

}
