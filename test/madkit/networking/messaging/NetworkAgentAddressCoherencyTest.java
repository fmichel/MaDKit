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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Message;
import madkit.message.ObjectMessage;
import madkit.testing.util.agent.ForEverSendRepliesWithTheSameMessageAgent;
import madkit.testing.util.agent.NormalAgent;

/**
 * @author Fabien Michel
 */
public class NetworkAgentAddressCoherencyTest extends JunitMadkit {

    @Test
    public void isCorrectlyLocalAfterTravelingTheNetwork() {
	addMadkitArgs(BooleanOption.network.toString(), LevelOption.kernelLogLevel.toString(), "ALL", LevelOption.networkLogLevel.toString(), "FINE");
	launchTest(new NormalAgent() {

	    protected void activate() {
		super.activate();
		assertTrue(isKernelOnline());
		launchExternalNetworkInstance(NetworkMessageAgent.class);
		Message message = waitNextMessage(10000);
		getLogger().info(message.toString());
		assertFalse(message.getSender().isFrom(getKernelAddress()));
		AgentAddress local = getAgentAddressIn(COMMUNITY, GROUP, ROLE);
		assertTrue(local.isFrom(getKernelAddress()));
		final AgentAddress receiver = message.getReceiver();
		assertTrue(receiver.equals(local));
		assertTrue(receiver.isFrom(getKernelAddress()));
		cleanHelperMDKs();
	    }
	});
    }

    @Test
    public void couldBeUsedLocallyAfterTravelingTheNetwork() {
	addMadkitArgs(BooleanOption.network.toString(), LevelOption.kernelLogLevel.toString(), "ALL"
	// ,LevelOption.networkLogLevel.toString(), "FINEST"
		, BooleanOption.network.toString());
	launchTest(new NormalAgent() {

	    @SuppressWarnings("unchecked")
	    protected void activate() {
		super.activate();
		requestRole(COMMUNITY, GROUP, ROLE2);
		launchExternalNetworkInstance(ForEverSendRepliesWithTheSameMessageAgent.class);
		Message message = waitNextMessage(10000);
		getLogger().info(message.toString());
		getLogger().info("sender is local ?" + message.getSender().isFrom(getKernelAddress()));
		assertFalse(message.getSender().isFrom(getKernelAddress()));
		AgentAddress local = getAgentAddressIn(COMMUNITY, GROUP, ROLE2);
		assertTrue(local.isFrom(getKernelAddress()));
		message = sendMessageAndWaitForReply(COMMUNITY, GROUP, ROLE, new ObjectMessage<>(local), 10000);
		final AgentAddress receiver = message.getReceiver();
		assertFalse(message.getSender().isFrom(getKernelAddress()));
		assertTrue(receiver.isFrom(getKernelAddress()));

		getLogger().info(message.toString());
		ObjectMessage<AgentAddress> m = (ObjectMessage<AgentAddress>) message;
		final AgentAddress myAA = m.getContent();
		getLogger().info(local.toString());
		getLogger().info(myAA.toString());
		assertTrue(local.equals(myAA));
		assertTrue(myAA.isFrom(getKernelAddress()));
		sendMessage(myAA, new Message());
		assertNotNull(nextMessage());
		assertTrue(checkAgentAddress(myAA));
		assertEquals(ReturnCode.SUCCESS, leaveRole(COMMUNITY, GROUP, ROLE2));
		assertFalse(checkAgentAddress(myAA));
		cleanHelperMDKs();
	    }
	});
    }

    @Test
    public void isCorrectlyLocalAfterEncapsulatingAAInMessage() {
	addMadkitArgs(BooleanOption.network.toString(), LevelOption.kernelLogLevel.toString(), "ALL", LevelOption.networkLogLevel.toString(), "FINE");
	launchTest(new NormalAgent() {

	    @SuppressWarnings("unchecked")
	    protected void activate() {
		super.activate();
		assertTrue(isKernelOnline());
		launchExternalNetworkInstance(ForEverSendRepliesWithTheSameMessageAgent.class);
		Message message = waitNextMessage(10000);
		getLogger().info(message.toString());
		getLogger().info("sender is local ?" + message.getSender().isFrom(getKernelAddress()));
		assertFalse(message.getSender().isFrom(getKernelAddress()));
		AgentAddress local = getAgentAddressIn(COMMUNITY, GROUP, ROLE);
		assertTrue(local.isFrom(getKernelAddress()));
		message = sendMessageAndWaitForReply(COMMUNITY, GROUP, ROLE, new ObjectMessage<>(local), 10000);
		final AgentAddress receiver = message.getReceiver();
		assertTrue(receiver.equals(local));
		assertTrue(receiver.isFrom(getKernelAddress()));
		ObjectMessage<AgentAddress> m = (ObjectMessage<AgentAddress>) message;
		assertTrue(receiver.equals(m.getContent()));
		assertTrue(m.getContent().isFrom(getKernelAddress()));
		cleanHelperMDKs();
	    }
	});
    }

}
