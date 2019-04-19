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
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Message;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.15
 * @version 0.9
 * 
 */

public class SendReplyWithRoleTest extends JunitMadkit {

    @Test
    public void returnNotInGroup() {
	launchTestV2(new Replier() {

	    protected void activate() {
		super.activate();
		assertEquals(SUCCESS, leaveRole(COMMUNITY, GROUP, ROLE));
		assertEquals(NOT_IN_GROUP, sendReplyWithRole(nextMessage(), new Message(), ROLE));
	    }
	});
    }

    @Test
    public void returnInvalidAA() {
	launchTestV2(new Replier() {

	    protected void activate() {
		super.activate();
		target.leaveGroup(COMMUNITY, GROUP);
		assertEquals(INVALID_AGENT_ADDRESS, sendReplyWithRole(nextMessage(), new Message(), ROLE));
	    }
	});
    }

    @Test
    public void returnSuccess() {
	launchTestV2(new Replier() {

	    protected void activate() {
		super.activate();
		assertEquals(SUCCESS, sendReplyWithRole(nextMessage(), new Message(), ROLE));
	    }
	});
    }

    @Test
    public void returnRoleNotHandled() {
	launchTestV2(new Replier() {

	    protected void activate() {
		super.activate();
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE2));
		assertEquals(SUCCESS, leaveRole(COMMUNITY, GROUP, ROLE));
		assertEquals(ROLE_NOT_HANDLED, sendReplyWithRole(nextMessage(), new Message(), ROLE));
	    }
	});
    }

    @Test
    public void wrongArg() {
	addMadkitArgs(LevelOption.kernelLogLevel.toString(),"ALL");
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		createDefaultCGR(this);
		assertEquals(ReturnCode.CANT_REPLY, sendReplyWithRole(new Message(), new Message(), ROLE));
	    }
	});
    }

    @Test
    public void wrongArgFromMessageSentFromAnObject() {
	addMadkitArgs(LevelOption.kernelLogLevel.toString(),"ALL");
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		createDefaultCGR(this);
		receiveMessage(new Message());
		assertEquals(ReturnCode.CANT_REPLY, sendReplyWithRole(nextMessage(), new Message(), ROLE));
	    }
	});
    }

    @Test
    public void nullArg() {
	launchTestV2(new Replier() {

	    protected void activate() {
		try {
		    assertEquals(SUCCESS, sendReplyWithRole(nextMessage(), null, ROLE));
		    noExceptionFailure();
		} catch (NullPointerException e) {
		    throw e;
		}
	    }
	}, ReturnCode.AGENT_CRASH);

	launchTestV2(new Replier() {

	    protected void activate() {
		try {
		    assertEquals(SUCCESS, sendReplyWithRole(null, new Message(), ROLE));
		    noExceptionFailure();
		} catch (NullPointerException e) {
		    throw e;
		}
	    }
	}, ReturnCode.AGENT_CRASH);

    }

}
