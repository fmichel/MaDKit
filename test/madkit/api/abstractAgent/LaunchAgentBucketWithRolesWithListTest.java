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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.Test;

import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.AbstractAgent.State;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Scheduler;
import madkit.simulation.SimulationException;
import madkit.simulation.activator.GenericBehaviorActivator;
import madkit.testing.util.agent.FaultyAA;
import madkit.testing.util.agent.SimulatedAgent;
import madkit.testing.util.agent.SimulatedAgentThatLaunchesASimulatedAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */

public class LaunchAgentBucketWithRolesWithListTest extends JunitMadkit {

    GenericBehaviorActivator<AbstractAgent> buggy;
    static int size = 1001;

    @Before
    public void setUp() throws Exception {
	buggy = new GenericBehaviorActivator<>(COMMUNITY, GROUP, ROLE, "doIt");
    }

    @Test
    public void cannotLaunch() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		launchAgentBucket(FaultyAA.class.getName(), 2, COMMUNITY + "," + GROUP + "," + ROLE);
	    }
	}, ReturnCode.SUCCESS);
    }

    @Test
    public void nullArg() {
	addMadkitArgs(LevelOption.kernelLogLevel.toString(), Level.ALL.toString());
	launchTest(new AbstractAgent() {

	    protected void activate() {
		List<AbstractAgent> l = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
		    l.add(null);
		}
		launchAgentBucket(l, COMMUNITY + "," + GROUP + "," + ROLE);
	    }
	}, ReturnCode.AGENT_CRASH);
    }

    @Test
    public void wrongCGR() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		getLogger().setLevel(Level.OFF);
		try {
		    launchAgentBucket(FaultyAA.class.getName(), 2, COMMUNITY + "," + GROUP + "," + ROLE);
		    JunitMadkit.noExceptionFailure();
		}
		catch(IllegalArgumentException e) {
		    throw e;
		}
	    }
	}, ReturnCode.AGENT_CRASH);
    }

    @Test
    public void returnSuccess() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		getLogger().setLevel(Level.OFF);
		List<SimulatedAgent> l = new ArrayList<>();
		for (int i = 0; i < size; i++) {
		    l.add(new SimulatedAgent());
		}
		launchAgentBucket(l, COMMUNITY + "," + GROUP + "," + ROLE);
		testAgents(l);
		assertEquals(size, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
	    }
	});
    }

    @Test
    public void returnSuccessWithInsideLaunches() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		List<SimulatedAgent> l = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
		    l.add(new SimulatedAgentThatLaunchesASimulatedAgent());
		}
		launchAgentBucket(l, COMMUNITY + "," + GROUP + "," + ROLE);
		testAgents(l);
		assertEquals(4, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
	    }
	});
    }

    @Test
    public void testBucketRequestRole() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		List<AbstractAgent> l = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
		    l.add(new AbstractAgent() {

			@Override
			protected void activate() {
			    assertEquals(ReturnCode.IGNORED, requestRole(COMMUNITY, GROUP, ROLE2, null));
			    bucketModeCreateGroup(COMMUNITY2, GROUP2, false, null);
			    assertEquals(ReturnCode.SUCCESS, bucketModeRequestRole(COMMUNITY2, GROUP2, ROLE2, null));
			}
		    });
		}
		launchAgentBucket(l, COMMUNITY + "," + GROUP + "," + ROLE);
		testAgentsRoles(l);
		assertEquals(5, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
	    }
	});
    }

    @Test
    public void inScheduledAgent() {
	launchTest(new Scheduler() {

	    protected void activate() {
		GenericBehaviorActivator<AbstractAgent> test = new GenericBehaviorActivator<>(COMMUNITY, GROUP, ROLE, "launchAgentBucketWithRoles");
		launchAgent(new SimulatedAgent());
		addActivator(test);
		try {
		    test.execute();
		}
		catch(SimulationException e) {
		    e.printStackTrace();
		    throw e;
		}
	    }
	}, ReturnCode.SUCCESS);
    }

    public static void testAgents(List<? extends AbstractAgent> l) {
	for (AbstractAgent abstractAgent : l) {
	    assertTrue(abstractAgent.isAlive());
	    assertEquals(State.ACTIVATED, abstractAgent.getState());
	    assertTrue(((SimulatedAgent) abstractAgent).goneThroughActivate());
	}
    }

    public static void testAgentsRoles(List<? extends AbstractAgent> l) {
	for (AbstractAgent abstractAgent : l) {
	    assertTrue(abstractAgent.isAlive());
	    ReturnCode requestRole = abstractAgent.requestRole(COMMUNITY, GROUP, ROLE);
	    System.err.println(requestRole);
	    assertEquals(ReturnCode.ROLE_ALREADY_HANDLED, requestRole);
	    requestRole = abstractAgent.requestRole(COMMUNITY, GROUP, ROLE2);
	    System.err.println(requestRole);
	    assertEquals(ReturnCode.SUCCESS, requestRole);
	    requestRole = abstractAgent.requestRole(COMMUNITY2, GROUP2, ROLE2);
	    System.err.println(requestRole);
	    assertEquals(ReturnCode.ROLE_ALREADY_HANDLED, requestRole);
	}
    }

}
