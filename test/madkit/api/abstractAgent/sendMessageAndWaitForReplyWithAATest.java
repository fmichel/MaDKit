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

import static madkit.kernel.AbstractAgent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.logging.Level;

import org.junit.Test;

import madkit.agr.DefaultMaDKitRoles;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Message;
import madkit.message.StringMessage;
import madkit.testing.util.agent.ForEverReplierAgent;
import madkit.testing.util.agent.NormalAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.8
 * @version 0.9
 * 
 */
@SuppressWarnings("all")
public class sendMessageAndWaitForReplyWithAATest extends JunitMadkit {

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
	launchTestV2(new NormalAgent() {
	    protected void activate() {
		getLogger().setLevel(Level.ALL);
		assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
		// assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
		assertEquals(SUCCESS, launchAgent(target3));

		assertEquals(SUCCESS, sendMessage(COMMUNITY, GROUP, ROLE, new Message()));
		assertNotNull(waitNextMessage(100));
		cleanHelperAgents();
	    }
	});
    }

    @Test
    public void sendReplyAndWaitForReply() {
	addMadkitArgs(LevelOption.kernelLogLevel.toString(),"ALL");
	launchTestV2(new NormalAgent() {
	    protected void activate() {
		super.activate();
		assertEquals(SUCCESS, launchAgent(new ForEverReplierAgent()));
		Message m = sendMessageAndWaitForReply(COMMUNITY, GROUP, ROLE, new Message());
		m = sendReplyAndWaitForReply(m, new Message());
		assertNotNull(m);
		m = sendReplyAndWaitForReply(m, new Message());
		assertNotNull(m);
		cleanHelperAgents();
	    }
	});
    }

    @Test
    public void sendReplyOnWaitNextMessage() {
	addMadkitArgs(LevelOption.kernelLogLevel.toString(),"ALL");
	launchTestV2(new NormalAgent() {
	    protected void activate() {
		super.activate();
		assertEquals(SUCCESS, launchAgent(new ForEverReplierAgent()));
		sendReply(waitNextMessage(), new Message());
		sendReply(waitNextMessage(), new Message());
		Message m = waitNextMessage();
		assertEquals(getAgentAddressIn(COMMUNITY, GROUP, ROLE), m.getReceiver());
		cleanHelperAgents();
	    }
	});
    }

    @Test
    public void returnSuccess() {
	launchTestV2(new NormalAgent() {
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
		assertEquals(INVALID_AGENT_ADDRESS, sendMessage(aa, new Message()));
		assertNull(sendMessageAndWaitForReply(aa, new Message()));
		cleanHelperAgents();
	    }
	});
    }

    @Test
    public void returnSuccessOnCandidateRole() {
	launchTestV2(new NormalAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, launchAgent(target2));

		// Without role
		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE);
		assertNotNull(aa);
		Message m = sendMessageAndWaitForReply(aa, new Message());
		assertNotNull(m);
		assertEquals("reply", ((StringMessage) m).getContent());
		assertEquals(DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE, m.getReceiver().getRole());
		assertEquals(DefaultMaDKitRoles.GROUP_MANAGER_ROLE, m.getSender().getRole());

		// With role
		m = sendMessageWithRoleAndWaitForReply(aa, new Message(), DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE);
		assertNotNull(m);
		assertEquals("reply2", ((StringMessage) m).getContent());
		assertEquals(DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE, m.getReceiver().getRole());
		assertEquals(DefaultMaDKitRoles.GROUP_MANAGER_ROLE, m.getSender().getRole());
		cleanHelperAgents();
	    }
	});
    }

    @Test
    public void returnInvalidAA() {
	launchTestV2(new NormalAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		assertEquals(SUCCESS, launchAgent(target));
		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
		assertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
		assertNull(sendMessageAndWaitForReply(aa, new Message()));// INVALID_AGENT_ADDRESS
		// warning
		assertEquals(INVALID_AGENT_ADDRESS, sendMessage(aa, new Message()));

		// With role
		assertNull(sendMessageWithRoleAndWaitForReply(aa, new Message(), ROLE));// INVALID_AGENT_ADDRESS
		cleanHelperAgents();
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

		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
		assertNull(sendMessageWithRoleAndWaitForReply(aa, new Message(), dontExist()));// not
		// role
		// warning
		assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
		assertNull(sendMessageAndWaitForReply(aa, new Message()));// not in
		// group
		// warning
		cleanHelperAgents();
	    }
	});
    }

    @Test
    public void nullArgs() {
	launchTestV2(new NormalAgent() {
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
		cleanHelperAgents();
	    }
	});
    }

}
