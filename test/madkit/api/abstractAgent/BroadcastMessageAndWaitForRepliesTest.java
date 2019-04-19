/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.logging.Level;

import org.junit.Test;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Message;
import madkit.message.StringMessage;
import madkit.testing.util.agent.NormalAgent;

/**
 * @author Fabien Michel
 */
public class BroadcastMessageAndWaitForRepliesTest extends JunitMadkit {

    final Agent target = new NormalAgent() {
	protected void activate() {
	    assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
	}

	protected void live() {
	    sendReply(waitNextMessage(), new StringMessage("reply"));
	    sendReply(waitNextMessage(), new StringMessage("reply2"));
	}
    };

    // sends the same message as reply
    final Agent target3 = new NormalAgent() {
	protected void activate() {
	    assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
	}

	protected void live() {
	    Message m = waitNextMessage();
	    sendReply(m, m);
	    waitNextMessage();// do not die !
	}
    };

    final Agent target2 = new NormalAgent() {
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
	launchTestV2(new NormalAgent() {
	    protected void activate() {
		getLogger().setLevel(Level.ALL);
		assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		assertEquals(SUCCESS, launchAgent(target3));
		assertEquals(1, broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), null, 100).size());
		killAgent(target3);
	    }
	});
    }

    @Test
    public void returnAllSuccess() {
	launchTestV2(new NormalAgent() {
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
	launchTestV2(new NormalAgent() {
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
	launchTestV2(new NormalAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent()));
		assertEquals(SUCCESS, launchAgent(new sendReplyInLiveAgent(1000)));
		assertEquals(1, broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), null, 10).size());
		assertEquals(1, broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), null, 10).size());
	    }
	});
    }

    @Test
    public void returnBadCGR() {
	launchTestV2(new NormalAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		assertEquals(SUCCESS, launchAgent(target));

		@SuppressWarnings("unused")
		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
		assertNull(broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), dontExist(), 100));// not
		// role
		// warning
		assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
		assertNull(broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), dontExist(), 100));// not
		// role
		// warning
		killAgent(target);
	    }
	});
    }

    @Test
    public void returnNotCGR() {
	launchTestV2(new NormalAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		assertEquals(SUCCESS, launchAgent(target));

		@SuppressWarnings("unused")
		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
		assertNull(broadcastMessageWithRoleAndWaitForReplies(dontExist(), GROUP, ROLE, new Message(), null, 100));
		assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
		assertNull(broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, dontExist(), ROLE, new Message(), null, 100));
		assertNull(broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, dontExist(), new Message(), null, 100));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		assertNull(broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, new Message(), dontExist(), 100));
		killAgent(target);
	    }
	});
    }

    @Test
    public void nullCommunity() {
	launchTestV2(new NormalAgent() {
	    protected void activate() {
		try {
		    broadcastMessageWithRoleAndWaitForReplies(null, dontExist(), dontExist(), new Message(), null, 0);
		    noExceptionFailure();
		} catch (NullPointerException e) {
		    throw e;
		}
	    }
	}, AGENT_CRASH);
    }

    @Test
    public void nullGroup() {
	launchTestV2(new NormalAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		try {
		    broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, null, dontExist(), new Message(), null, 0);
		    noExceptionFailure();
		} catch (NullPointerException e) {
		    throw e;
		}
	    }
	}, AGENT_CRASH);
    }

    @Test
    public void nullRole() {
	launchTestV2(new NormalAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
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
	launchTestV2(new NormalAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		launchAgent(target);
		try {
		    broadcastMessageWithRoleAndWaitForReplies(COMMUNITY, GROUP, ROLE, null, null, 0);
		    noExceptionFailure();
		} catch (NullPointerException e) {
		    killAgent(target);
		    throw e;
		}
	    }
	});
    }
}

class sendReplyInLiveAgent extends Agent {
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