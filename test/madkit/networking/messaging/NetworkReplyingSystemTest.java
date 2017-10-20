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
package madkit.networking.messaging;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.logging.Level;

import org.junit.Test;

import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Message;
import madkit.testing.util.agent.ForEverAgent;
import madkit.testing.util.agent.ForEverReplierAgent;
import madkit.testing.util.agent.NormalAgent;
import madkit.testing.util.agent.OneReplyAndQuitAgent;

/**
 * @author Fabien Michel
 */
public class NetworkReplyingSystemTest extends JunitMadkit {

    @Test
    public void ping() {
	addMadkitArgs(BooleanOption.network.toString(), LevelOption.kernelLogLevel.toString(), "ALL", LevelOption.networkLogLevel.toString(), "FINE");
	launchTest(new NormalAgent() {

	    protected void activate() {
		assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, true));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		final ForEverAgent agent = new ForEverAgent();
		launchAgent(agent);
		getLogger().setLevel(Level.ALL);
		assertTrue(isKernelOnline());
		Message m = new Message();
		for (int i = 0; i < 10; i++) {
		    sendMessage(COMMUNITY, GROUP, ROLE, new Message());
		}
		killAgent(agent);
		launchExternalNetworkInstance(NetworkMessageAgent.class);
		pause(5000);
		Message message = new Message();
		sendMessage(COMMUNITY, GROUP, ROLE, message);
		getLogger().info(message.toString());
		Message m2 = sendMessageAndWaitForReply(COMMUNITY, GROUP, ROLE, m, 10000);
		cleanHelperMDKs(1000);
		System.err.println(m);
		assertNull(m2);
		System.err.println(nextMessage());
		System.err.println(nextMessage());
		System.err.println(nextMessage());
		System.err.println(nextMessage());
	    }
	});
    }

    @Test
    public void sendReplyTest() {
	addMadkitArgs(BooleanOption.network.toString(), LevelOption.kernelLogLevel.toString(), "ALL", LevelOption.networkLogLevel.toString(), "FINE");
	launchTest(new NormalAgent() {

	    protected void activate() {
		super.activate();
		assertTrue(isKernelOnline());
		launchExternalNetworkInstance(ForEverReplierAgent.class);
		waitNextMessage(10000);
		Message m = sendMessageAndWaitForReply(COMMUNITY, GROUP, ROLE, new Message());
		m = sendReplyAndWaitForReply(m, new Message());
		assertNotNull(m);
		m = sendReplyAndWaitForReply(m, new Message());
		assertNotNull(m);
	    }
	});
    }

    @Test
    public void sendReplyAfterDeconnection() {
	addMadkitArgs(BooleanOption.network.toString(), LevelOption.kernelLogLevel.toString(), "ALL", LevelOption.networkLogLevel.toString(), "FINE");
	launchTest(new NormalAgent() {

	    protected void activate() {
		super.activate();
		assertTrue(isKernelOnline());
		launchExternalNetworkInstance(OneReplyAndQuitAgent.class);
		waitNextMessage(10000);
		Message m = sendMessageAndWaitForReply(COMMUNITY, GROUP, ROLE, new Message());
		assertNotNull(m);
		cleanHelperMDKs(2000);
		sendMessage(m.getSender(), new Message());
	    }
	});
    }

}
