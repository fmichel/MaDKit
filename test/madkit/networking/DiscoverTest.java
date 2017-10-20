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
package madkit.networking;

import static org.junit.Assert.assertFalse;

import java.util.logging.Level;

import org.junit.Test;

import madkit.action.KernelAction;
import madkit.agr.NetworkCommunity;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */

public class DiscoverTest extends JunitMadkit {

    protected static final int OTHERS = 6;

    // @After
    // public void clean(){
    // cleanHelperMDKs();
    // }

    // TODO use junit 4.12
    // @Rule public final TestRule timeout = Timeout.builder()
    // .withTimeout(10, TimeUnit.SECONDS)
    // .withLookingForStuckThread(true)
    // .build();

    @Test
    public void multipleConnectionTest() {
	cleanHelperMDKs(2000);
	addMadkitArgs(BooleanOption.network.toString(), LevelOption.networkLogLevel.toString(), "FINE", LevelOption.madkitLogLevel.toString(), Level.ALL.toString());
	launchTest(new AbstractAgent() {

	    @Override
	    protected void activate() {
		KernelAction.LAUNCH_NETWORK.getActionFor(this).actionPerformed(null);
		for (int i = 0; i < OTHERS; i++) {
		    launchMKNetworkInstance(Level.OFF);
		}
		pause(1000);
		testConnections(this);
	    }
	});
    }

    @Test
    public void multipleExternalConnectionTest() {
	cleanHelperMDKs(2000);
	addMadkitArgs(BooleanOption.network.toString(), LevelOption.networkLogLevel.toString(), "FINE", LevelOption.madkitLogLevel.toString(), Level.ALL.toString());
	launchTest(new AbstractAgent() {

	    @Override
	    protected void activate() {
		KernelAction.LAUNCH_NETWORK.getActionFor(this).actionPerformed(null);
		for (int i = 0; i < OTHERS; i++) {
		    launchExternalNetworkInstance();
		}
		testConnections(this);
	    }
	});
    }

    /**
     * @param agent
     * 
     */
    private void testConnections(AbstractAgent agent) {
	agent.getLogger().setLevel(Level.INFO);
	checkConnectedIntancesNb(agent, OTHERS + 1);
	// List<AgentAddress> l = agent.getAgentsWithRole(NetworkCommunity.NAME, NetworkCommunity.Groups.NETWORK_AGENTS,
	// NetworkCommunity.Roles.NET_AGENT);
	// for (AgentAddress agentAddress : l) {
	// System.err.println(agentAddress);
	// }
	// assertEquals(OTHERS+1, l.size());
	KernelAction.STOP_NETWORK.getActionFor(agent).actionPerformed(null);
	startTimer();
	do {
	    pause(500);
	}
	while (stopTimer("") < 10000 && agent.isCommunity(NetworkCommunity.NAME));

	// System.err.println(agent.getOrganizationSnapShot(true));
	// not connected
	assertFalse(agent.isCommunity(NetworkCommunity.NAME));

	// second round
	KernelAction.LAUNCH_NETWORK.getActionFor(agent).actionPerformed(null);
	pause(300);
	checkConnectedIntancesNb(agent, OTHERS + 1);
	cleanHelperMDKs();
	checkConnectedIntancesNb(agent, 1);
	KernelAction.EXIT.getActionFor(agent).actionPerformed(null);
    }

}
