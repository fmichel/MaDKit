
package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import org.testng.annotations.Test;

import madkit.messages.StringMessage;
import madkit.messaging.ForEverReplierAgent;
import madkit.test.agents.CGRAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.8
 * @version 0.9
 * 
 */
@SuppressWarnings("all")
public class waitAnswerTest extends JunitMadkit {

	final Agent target = new Agent() {
		AgentAddress aa;

		protected void onActivation() {
			threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
			aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
			threadAssertNotNull(aa);
			threadAssertEquals(SUCCESS, send(new Message(), aa));
			threadAssertEquals(SUCCESS, send(new Message(), aa));
		}

		protected void onLiving() {
			waitNextMessage();// waiting the start signal
			reply(new StringMessage("reply"), waitNextMessage());
			threadAssertEquals(SUCCESS, send(new Message(), aa));
			threadAssertEquals(SUCCESS, send(new Message(), aa));
			reply(new StringMessage("reply2"), waitNextMessage());
		}
	};

	// sends the same message as reply
	final Agent target3 = new Agent() {
		protected void onActivation() {
			threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		}

		protected void onLiving() {
			Message m = waitNextMessage();
			reply(m, m);
			waitNextMessage();// do not die !
		}
	};

	final Agent target2 = new Agent() {
		protected void onActivation() {
			threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
		}

		protected void onLiving() {
			reply(new StringMessage("reply"), waitNextMessage());
			reply(new StringMessage("reply2"), waitNextMessage());
		}
	};

	@Test
	public void replyWithSameMessage() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				threadAssertEquals(SUCCESS, launchAgent(target3));

				threadAssertEquals(SUCCESS, send(new Message(), COMMUNITY, GROUP, ROLE));
				threadAssertNotNull(waitNextMessage(100));
			}
		});
	}

	@Test
	public void replyAndWaitForReply() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				threadAssertEquals(SUCCESS, launchAgent(new ForEverReplierAgent()));
				Message m = sendWaitReply(new Message(), COMMUNITY, GROUP, ROLE, null);
				Message reply = new Message();
				reply(reply, m);
				m = waitAnswer(reply);
				threadAssertNotNull(m);
				reply = new Message();
				reply(reply, m);
				m = waitAnswer(reply);
				threadAssertNotNull(m);
			}
		});
	}

//    @Test
//    public void replyOnWaitNextMessage() {
//	launchTestedAgent(new CGRAgent() {
//	    protected void activate() {
//		super.activate();
//		threadAssertEquals(SUCCESS, launchAgent(new ForEverReplierAgent()));
//		reply(waitNextMessage(), new Message());
//		reply(waitNextMessage(), new Message());
//		Message m = waitNextMessage();
//		threadAssertEquals(getAgentAddressIn(COMMUNITY, GROUP, ROLE), m.getReceiver());
//		cleanHelperAgents();
//	    }
//	});
//    }
//
//    @Test
//    public void returnSuccess() {
//	launchTestedAgent(new CGRAgent() {
//	    protected void activate() {
//		threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
//		threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
//		threadAssertEquals(SUCCESS, launchAgent(target));
//
//		assertFalse(this.isMessageBoxEmpty());
//		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
//		threadAssertNotNull(aa);
//
//		// time out but gives the start signal
//		assertNull(sendAndWaitForReply(aa, new Message(), 100));
//
//		// Without role
//		Message m = sendAndWaitForReply(aa, new Message());
//		threadAssertNotNull(m);
//		threadAssertEquals("reply", ((StringMessage) m).getContent());
//		threadAssertEquals(ROLE, m.getReceiver().getRole());
//
//		assertFalse(this.isMessageBoxEmpty());
//
//		// With role
//		m = sendAndWaitForReply(aa, new Message());
//		threadAssertNotNull(m);
//		threadAssertEquals("reply2", ((StringMessage) m).getContent());
//		threadAssertEquals(ROLE, m.getReceiver().getRole());
//
//		assertNotNull(next());
//		assertNotNull(next());
//		assertNotNull(next());
//		assertNotNull(next());
//		assertNull(next());
//		sleep(100);
//		threadAssertEquals(INVALID_AGENT_ADDRESS, send(aa, new Message()));
//		assertNull(sendAndWaitForReply(aa, new Message()));
//		cleanHelperAgents();
//	    }
//	});
//    }
//
//    @Test
//    public void returnSuccessOnCandidateRole() {
//	launchTestedAgent(new CGRAgent() {
//	    protected void activate() {
//		threadAssertEquals(SUCCESS, launchAgent(target2));
//
//		// Without role
//		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE);
//		threadAssertNotNull(aa);
//		Message m = sendAndWaitForReply(aa, new Message());
//		threadAssertNotNull(m);
//		threadAssertEquals("reply", ((StringMessage) m).getContent());
//		threadAssertEquals(DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE, m.getReceiver().getRole());
//		threadAssertEquals(DefaultMaDKitRoles.GROUP_MANAGER_ROLE, m.getSender().getRole());
//
//		// With role
//		m = sendWithRoleAndWaitForReply(aa, new Message(), DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE);
//		threadAssertNotNull(m);
//		threadAssertEquals("reply2", ((StringMessage) m).getContent());
//		threadAssertEquals(DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE, m.getReceiver().getRole());
//		threadAssertEquals(DefaultMaDKitRoles.GROUP_MANAGER_ROLE, m.getSender().getRole());
//		cleanHelperAgents();
//	    }
//	});
//    }
//
//    @Test
//    public void returnInvalidAA() {
//	launchTestedAgent(new CGRAgent() {
//	    protected void activate() {
//		threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
//		threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
//		threadAssertEquals(SUCCESS, launchAgent(target));
//		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
//		threadAssertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
//		assertNull(sendAndWaitForReply(aa, new Message()));// INVALID_AGENT_ADDRESS
//		// warning
//		threadAssertEquals(INVALID_AGENT_ADDRESS, send(aa, new Message()));
//
//		// With role
//		assertNull(sendWithRoleAndWaitForReply(aa, new Message(), ROLE));// INVALID_AGENT_ADDRESS
//		cleanHelperAgents();
//	    }
//	});
//    }
//
//    @Test
//    public void returnBadCGR() {
//	launchTestedAgent(new CGRAgent() {
//	    protected void activate() {
//		threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
//		threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
//		threadAssertEquals(SUCCESS, launchAgent(target));
//
//		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
//		assertNull(sendWithRoleAndWaitForReply(aa, new Message(), dontExist()));// not
//		// role
//		// warning
//		threadAssertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
//		assertNull(sendAndWaitForReply(aa, new Message()));// not in
//		// group
//		// warning
//		cleanHelperAgents();
//	    }
//	});
//    }
//
//    @Test
//    public void nullArgs() {
//	launchTestedAgent(new CGRAgent() {
//	    protected void activate() {
//		threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
//		threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
//		threadAssertEquals(SUCCESS, launchAgent(target));
//		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
//		try {
//		    sendAndWaitForReply(null, null);
//		    noExceptionFailure();
//		} catch (NullPointerException e) {
//		    e.printStackTrace();
//		}
//		try {
//		    sendAndWaitForReply(aa, null);
//		    noExceptionFailure();
//		} catch (NullPointerException e) {
//		    e.printStackTrace();
//		}
//		try {
//		    sendAndWaitForReply(null, new Message());
//		    noExceptionFailure();
//		} catch (NullPointerException e) {
//		    e.printStackTrace();
//		}
//		cleanHelperAgents();
//	    }
//	});
//    }

}
