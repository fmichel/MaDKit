package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.logging.Level;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Message;
import madkit.message.StringMessage;

import org.junit.Test;

@SuppressWarnings("serial")
public class BroadcastMessageAndWaitForRepliesTest extends JunitMadkit {

	final Agent target = new Agent() {
		protected void activate() {
			assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		}

		protected void live() {
			sendReply(waitNextMessage(), new StringMessage("reply"));
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
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target3));
				assertEquals(1, broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), null, 100).size());
			}
		});
	}

	@Test
	public void returnAllSuccess() {
		launchTest(new Agent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent()));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent()));
				assertEquals(2, broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), null, 100).size());
				assertEquals(2, broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), null, 100).size());
			}
		});
	}

	@Test
	public void returnNullOnTimeout() {
		launchTest(new Agent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent()));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent()));
				assertNull(broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), null, 0));
				assertNull(broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), null, -1));
			}
		});
	}

	@Test
	public void returnOneSuccess() {
		launchTest(new Agent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent()));
				assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent(1000)));
				assertEquals(1, broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), null, 100).size());
				assertEquals(1, broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), null, 100).size());
			}
		});
	}

	// @Test
	// public void returnSuccessOnCandidateRole(){
	// launchTest(new Agent(){
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
		launchTest(new Agent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));

				@SuppressWarnings("unused")
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertNull(broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), aa(), 100));// not
																																							// role
																																							// warning
				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertNull(broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), aa(), 100));// not
																																							// role
																																							// warning

			}
		});
	}

	@Test
	public void returnNotCGR() {
		launchTest(new Agent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));

				@SuppressWarnings("unused")
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertNull(broadcastMessageWithRoleAndWaitForReplies(aa(), GROUP, ROLE, new Message(), null, 100));
				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertNull(broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, aa(), ROLE, new Message(), null, 100));
				assertNull(broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, aa(), new Message(), null, 100));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertNull(broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), aa(), 100));

			}
		});
	}

	@Test
	public void nullCommunity() {
		launchTest(new Agent() {
			protected void activate() {
				try {
					broadcastMessageWithRoleAndWaitForReplies(null, aa(), aa(), new Message(), null, 0);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

	@Test
	public void nullGroup() {
		launchTest(new Agent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));
				try {
					broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, null, aa(), new Message(), null, 0);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

	@Test
	public void nullRole() {
		launchTest(new Agent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));
				try {
					broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, null, new Message(), null, 0);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

	@Test
	public void nullMessage() {
		launchTest(new Agent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(target));
				try {
					broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, null, null, 0);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

}

class sendReplyInLiveAgent extends Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int time;

	public sendReplyInLiveAgent(int timeToReply) {
		time = timeToReply;
	}

	public sendReplyInLiveAgent() {
		time = 0;
	}

	protected void activate() {
		assertEquals(SUCCESS, requestRole(JunitMadkit.COMMUNITY, JunitMadkit.GROUP, JunitMadkit.ROLE));
	}

	protected void live() {
		pause(time);
		sendReply(waitNextMessage(), new StringMessage("reply " + hashCode()));
		pause(time);
		sendReply(waitNextMessage(), new StringMessage("reply " + hashCode()));
	}
}