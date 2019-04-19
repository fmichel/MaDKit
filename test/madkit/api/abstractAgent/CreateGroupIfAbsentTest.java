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

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Gatekeeper;
import madkit.kernel.JunitMadkit;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.16
 * @version 0.9
 * 
 */
@SuppressWarnings("all")
public class CreateGroupIfAbsentTest extends JunitMadkit {

    final Gatekeeper gi = new Gatekeeper() {
	@Override
	public boolean allowAgentToTakeRole(String requesterID, String roleName, Object memberCard) {
	    return false;
	}
    };

    @Test
    public void returnFalse() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
		assertFalse(createGroupIfAbsent(COMMUNITY, GROUP));
		assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
		assertTrue(createGroupIfAbsent(COMMUNITY, GROUP));
	    }
	});
    }

    @Test
    public void communityIsNull() {
	addMadkitArgs("--kernelLogLevel", "ALL");
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		try {
		    createGroupIfAbsent(null, dontExist());
		    noExceptionFailure();
		} catch (NullPointerException e) {
		    throw e;
		}
	    }
	},ReturnCode.AGENT_CRASH);
    }

    @Test
    public void groupIsNull() {
	addMadkitArgs("--kernelLogLevel", "ALL");
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		try {
		    createGroupIfAbsent(dontExist(), null);
		    noExceptionFailure();
		} catch (NullPointerException e) {
		    throw e;
		}
	    }
	},ReturnCode.AGENT_CRASH);
    }

    @Test
    public void nullArgs() {
	addMadkitArgs("--kernelLogLevel", "ALL");
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		try {
		    createGroupIfAbsent(null, null,true);
		    noExceptionFailure();
		} catch (NullPointerException e) {
		    e.printStackTrace();
		}
		try {
		    createGroupIfAbsent(dontExist(), null, false, null);
		    noExceptionFailure();
		} catch (NullPointerException e) {
		    throw e;
		}
	    }
	},ReturnCode.AGENT_CRASH);
    }

    @Test
    public void createGroupAndLeaveAndCreate() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertFalse(isCommunity(COMMUNITY));
		assertFalse(isGroup(COMMUNITY, GROUP));
		assertTrue(createGroupIfAbsent(COMMUNITY, GROUP));
		assertTrue(isCommunity(COMMUNITY));
		assertTrue(isGroup(COMMUNITY, GROUP));
		assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
		assertTrue(createGroupIfAbsent(COMMUNITY, GROUP));
		assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
		assertFalse(isCommunity(COMMUNITY));
		assertFalse(isGroup(COMMUNITY, GROUP));
	    }
	});
    }

}
