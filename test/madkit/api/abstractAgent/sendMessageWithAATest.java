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
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_ROLE;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import madkit.agr.DefaultMaDKitRoles;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Message;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class sendMessageWithAATest extends JunitMadkit {

    final AbstractAgent target = new AbstractAgent() {
	protected void activate() {
	    assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
	    assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
	}
    };

    @Test
    public void returnSuccess() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, launchAgent(target));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));

		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
		assertNotNull(aa);

		// Without role
		assertEquals(SUCCESS, sendMessage(aa, new Message()));
		Message m = target.nextMessage();
		assertNotNull(m);
		assertEquals(ROLE, m.getReceiver().getRole());

		// With role
		assertEquals(SUCCESS, sendMessageWithRole(aa, new Message(), ROLE));
		m = target.nextMessage();
		assertNotNull(m);
		assertEquals(ROLE, m.getReceiver().getRole());
	    }
	});
    }

    @Test
    public void returnSuccessOnCandidateRole() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, launchAgent(target));

		// Without role
		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE);
		assertNotNull(aa);
		assertEquals(SUCCESS, sendMessage(aa, new Message()));
		Message m = target.nextMessage();
		assertNotNull(m);
		assertEquals(DefaultMaDKitRoles.GROUP_MANAGER_ROLE, m.getReceiver().getRole());
		assertEquals(DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE, m.getSender().getRole());

		// With role
		aa = getAgentWithRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE);
		assertNotNull(aa);
		assertEquals(SUCCESS, sendMessageWithRole(aa, new Message(), DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE));
		m = target.nextMessage();
		assertNotNull(m);
		assertEquals(DefaultMaDKitRoles.GROUP_MANAGER_ROLE, m.getReceiver().getRole());
		assertEquals(DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE, m.getSender().getRole());
	    }
	});
    }

    @Test
    public void returnInvalidAA() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, launchAgent(target));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
		assertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
		assertEquals(INVALID_AGENT_ADDRESS, sendMessage(aa, new Message()));

		// With role
		assertEquals(INVALID_AGENT_ADDRESS, sendMessageWithRole(aa, new Message(), ROLE));
	    }
	});
    }

    @Test
    public void returnNotInGroup() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, launchAgent(target));
		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
		assertEquals(NOT_IN_GROUP, sendMessageWithRole(aa, new Message(), ROLE));
		assertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
		assertEquals(INVALID_AGENT_ADDRESS, sendMessage(aa, new Message()));
		assertEquals(NOT_ROLE, sendMessage(COMMUNITY, GROUP, ROLE, new Message()));

		// With role
	    }
	});
    }

    @Test
    public void returnRoleNotHandled() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, launchAgent(target));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));

		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
		assertEquals(ROLE_NOT_HANDLED, sendMessageWithRole(aa, new Message(), dontExist()));

	    }
	});
    }

    @Test
    public void nullArgs() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		try {
		    sendMessage(null, null);
		    noExceptionFailure();
		} catch (NullPointerException e) {
		    throw e;
		}
	    }
	}, ReturnCode.AGENT_CRASH);
    }

    @Test
    public void nullAA() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		try {
		    sendMessage(null, new Message());
		    noExceptionFailure();
		} catch (NullPointerException e) {
		    throw e;
		}
	    }
	}, ReturnCode.AGENT_CRASH);
    }

    @Test
    public void nullMessage() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, launchAgent(target));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
		try {
		    sendMessage(aa, null);
		    noExceptionFailure();
		} catch (NullPointerException e) {
		    throw e;
		}
	    }
	}, ReturnCode.AGENT_CRASH);
    }

}
