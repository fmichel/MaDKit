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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.logging.Level;

import org.junit.Test;

import madkit.agr.DefaultMaDKitRoles;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.AbstractAgent.State;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.testing.util.agent.SimulatedAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */

public class LaunchAgentBucketTest extends JunitMadkit {

    static int size = 1001;

    @Test
    public void returnSuccess() {
	for (int i = 0; i < 100; i++) {
	    // addMadkitArgs(LevelOption.kernelLogLevel.toString(),Level.ALL.toString());
	    launchTest(new AbstractAgent() {

		protected void activate() {
		    getLogger().setLevel(Level.OFF);
		    List<AbstractAgent> l = launchAgentBucket(SimulatedAgent.class.getName(), size);
		    assertEquals(size, l.size());
		    testAgents(l);
		    assertEquals(size, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
		}

	    });
	}
    }

    @Test
    public void withAnAgentClass() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		List<AbstractAgent> l = launchAgentBucket(Agent.class.getName(), size);
		assertNotNull(l);
		killAgent(l.get(0));
	    }
	});
    }

    @Test
    public void withAnAAClass() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		List<AbstractAgent> l = launchAgentBucket(SimulatedAgent.class.getName(), size);
		assertNotNull(l);
		killAgent(l.get(0));
	    }
	});
    }

    @Test
    public void massAALaunchWithBucketRoles() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		createGroup(COMMUNITY, GROUP);
		Thread t = new Thread(new Runnable() {

		    @Override
		    public void run() {
			launchAgent(new Agent() {

			    @Override
			    protected void activate() {
				pause(100);
				requestRole(COMMUNITY, GROUP, ROLE);
			    }

			    @Override
			    protected void live() {
				pause(100000);
			    }
			}, true);
		    }
		});
		t.start();
		System.err.println("begin");
		startTimer();
		launchAgentBucket(AbstractAgent.class.getName(), 1000000, COMMUNITY + "," + GROUP + "," + ROLE);
		try {
		    t.join();
		}
		catch(InterruptedException e) {
		    e.printStackTrace();
		}
		stopTimer("bucket launch time = ");
		System.err.println("done\n\n");
		requestRole(COMMUNITY, GROUP, ROLE);
		getLogger().setLevel(Level.OFF);
		assertEquals(1000002, getAgentsWithRole(COMMUNITY, GROUP, ROLE, true).size());
	    }
	});
    }

    @Test
    public void nullArg() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		try {
		    launchAgentBucket((String) null, size);
		    noExceptionFailure();
		}
		catch(NullPointerException e) {
		    throw e;
		}
	    }

	}, ReturnCode.AGENT_CRASH);
    }

    @Test
    public void classNotExist() {
	addMadkitArgs(LevelOption.kernelLogLevel.toString(), Level.ALL.toString());
	launchTest(new AbstractAgent() {

	    protected void activate() {
		launchAgentBucket("fake.fake", 2);
	    }

	});
    }

    private void testAgents(List<AbstractAgent> l) {
	for (AbstractAgent abstractAgent : l) {
	    assertTrue(abstractAgent.isAlive());
	    assertEquals(State.ACTIVATED, abstractAgent.getState());
	    assertTrue(((SimulatedAgent) abstractAgent).goneThroughActivate());
	}
    }

    @Test
    public void returnSuccessOn0() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		// getLogger().setLevel(Level.ALL);
		List<AbstractAgent> l = launchAgentBucket(SimulatedAgent.class.getName(), 0);
		assertEquals(0, l.size());
		assertEquals(null, getAgentsWithRole(COMMUNITY, GROUP, ROLE));
		testAgents(l);
	    }
	});
    }

    @Test
    public void returnSuccessOn1() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		List<AbstractAgent> l = launchAgentBucket(SimulatedAgent.class.getName(), 1);
		assertEquals(1, l.size());
		assertEquals(1, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
		testAgents(l);
	    }
	});
    }

    @Test
    public void moreCPUThanAgents() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		int nbOfAgents = 50;
		for (int j = 0; j < nbOfAgents; j++) {
		    for (int i = 1; i < Runtime.getRuntime().availableProcessors() * 2; i++) {
			List<AbstractAgent> l = launchAgentBucket(SimulatedAgent.class.getName(), j, i);
			assertEquals(j, l.size());
			final List<AgentAddress> agentsWithRole = getAgentsWithRole(COMMUNITY, GROUP, ROLE);
			if (agentsWithRole != null) {
			    assertEquals(j, agentsWithRole.size());
			}
			testAgents(l);
			destroyGroup(COMMUNITY, GROUP);
		    }
		}
	    }
	});
    }

    @Test
    public void returnSuccessWithName() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		getLogger().setLevel(Level.OFF);
		List<AbstractAgent> l = launchAgentBucket(SimulatedAgent.class.getName(), size, COMMUNITY + "," + GROUP + "," + ROLE);
		assertEquals(size, l.size());
		assertEquals(size, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
		// I am the manager
		assertNull(getAgentsWithRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE));
		assertNotNull(getAgentsWithRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE, true));
		testAgents(l);
	    }
	});
    }

}
