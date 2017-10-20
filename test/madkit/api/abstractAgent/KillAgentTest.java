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
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_KILLED;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_YET_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.logging.Level;

import org.junit.Test;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.testing.util.agent.DoItDuringLifeCycleAgent;
import madkit.testing.util.agent.FaultyAgent;
import madkit.testing.util.agent.KillTargetAgent;
import madkit.testing.util.agent.NormalLife;
import madkit.testing.util.agent.RandomT;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class KillAgentTest extends JunitMadkit {

    final Agent target = new Agent() {

	protected void activate() {
	    assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
	    assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
	}

	protected void live() {
	    pause(10000);
	    getLogger().info("finishing live");
	}
    };

    final AbstractAgent timeOutAgent = new AbstractAgent() {

	protected void activate() {
	    try {
		Thread.sleep(2000);
	    }
	    catch(InterruptedException e) {
		e.printStackTrace();
	    }
	}
    };

    @Test
    public void returnSuccess() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		assertEquals(SUCCESS, launchAgent(target));
		assertEquals(SUCCESS, killAgent(target));
	    }
	});
    }

    @Test
    public void returnSuccessAfterLaunchTimeOut() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		assertEquals(TIMEOUT, launchAgent(timeOutAgent, 1));
		assertEquals(SUCCESS, killAgent(timeOutAgent));
	    }
	});
    }

    @Test
    public void selfKillInActivate() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		SelfKillAgent a = new SelfKillAgent(true);
		assertEquals(SUCCESS, launchAgent(a));
		assertEquals(ALREADY_KILLED, killAgent(a));
		assertAgentIsTerminated(a);
	    }
	}, true);
    }

    @Test
    public void selfKillInActivateAndEnd() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		SelfKillAgent a = new SelfKillAgent(true, false, true);
		assertEquals(SUCCESS, launchAgent(a));
		assertAgentIsTerminated(a);
	    }
	}, true);
    }

    @Test
    public void selfKillInEnd() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		SelfKillAgent a = new SelfKillAgent(false, false, true);
		assertEquals(SUCCESS, launchAgent(a));
		pause(100);
		assertEquals(ALREADY_KILLED, killAgent(a));
		assertAgentIsTerminated(a);
	    }
	}, true);
    }

    @Test
    public void selfKill() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		assertEquals(SUCCESS, launchAgent(new SelfKillAgent(true), 1));
		assertEquals(SUCCESS, launchAgent(new SelfKillAgent(false, true), 1));
		assertEquals(SUCCESS, launchAgent(new SelfKillAgent(false, false, true), 1));
		assertEquals(SUCCESS, launchAgent(new SelfKillAgent(true, false, true), 1));
	    }
	});
    }

    @Test
    public void returnNOT_YET_LAUNCHEDAfterImmediateLaunch() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		assertEquals(TIMEOUT, launchAgent(timeOutAgent, 0));
		ReturnCode r = killAgent(timeOutAgent);
		assertTrue(NOT_YET_LAUNCHED == r || SUCCESS == r);
		pause(2000);
		if (r == NOT_YET_LAUNCHED) {
		    assertEquals(SUCCESS, killAgent(timeOutAgent));
		}
	    }
	});
    }

    @Test
    public void returnAlreadyKilled() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		AbstractAgent a = new FaultyAgent(true);
		assertEquals(AGENT_CRASH, launchAgent(a));
		assertEquals(ALREADY_KILLED, killAgent(a));
		a = new FaultyAgent(false, true);
		assertEquals(SUCCESS, launchAgent(a));
		pause(100);
		assertEquals(ALREADY_KILLED, killAgent(a));
	    }
	});
    }

    @Test
    public void agentCrash() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		AbstractAgent a = new FaultyAgent(true);
		assertEquals(AGENT_CRASH, launchAgent(a));
		pause(100);
	    }
	});
    }

    @Test
    public void massKill() {
	addMadkitArgs(LevelOption.agentLogLevel.toString(), "OFF");
	addMadkitArgs(LevelOption.kernelLogLevel.toString(), "OFF");
	launchTest(new AbstractAgent() {

	    protected void activate() {
		getLogger().setLevel(Level.INFO);
		int number = 600;
		ArrayList<AbstractAgent> list = new ArrayList<>(number);
		for (int i = 0; i < number; i++) {
		    if (i % 100 == 0)
			getLogger().info(i + " agents launched");
		    TimeOutAgent t = new TimeOutAgent();
		    list.add(t);
		    assertEquals(SUCCESS, launchAgent(t));
		}
		for (AbstractAgent a : list) {
		    ReturnCode r = killAgent(a);
		    assertTrue(ALREADY_KILLED == r || SUCCESS == r);
		}
	    }
	});
    }

    @Test
    public void returnTimeOut() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		assertEquals(TIMEOUT, launchAgent(timeOutAgent, 1));
		assertEquals(ALREADY_LAUNCHED, launchAgent(timeOutAgent));
		assertEquals(SUCCESS, killAgent(timeOutAgent));
		assertAgentIsTerminated(timeOutAgent);
	    }
	});
    }

    @Test
    public void returnAleradyLaunch() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		assertEquals(TIMEOUT, launchAgent(timeOutAgent, 1));
		assertEquals(ALREADY_LAUNCHED, launchAgent(timeOutAgent));
	    }
	});
    }

    @Test
    public void randomLaunchAndKill() {
	addMadkitArgs(LevelOption.agentLogLevel.toString(), "OFF", LevelOption.kernelLogLevel.toString(), "OFF", LevelOption.guiLogLevel.toString(), "OFF");
	launchTest(new AbstractAgent() {

	    protected void activate() {
		getLogger().info("******************* STARTING RANDOM LAUNCH & AGENT_KILL *******************\n");
		Agent a = (Agent) launchAgent(NormalLife.class.getName(), Math.random() < .5 ? true : false);
		assertNotNull(a);
		ReturnCode r = killAgent(a, (int) (Math.random() * 2));
		assertTrue(SUCCESS == r || TIMEOUT == r);
		Runnable job = new Runnable() {

		    @Override
		    public void run() {
			for (int i = 0; i < 20; i++) {
			    Agent agt = (Agent) launchAgent(NormalLife.class.getName(), Math.random() < .5 ? true : false);
			    assertNotNull(agt);
			    pause((int) (Math.random() * 1000));
			    ReturnCode r2 = killAgent(agt, (int) (Math.random() * 2));
			    assertTrue(SUCCESS == r2 || TIMEOUT == r2);
			}
		    }
		};
		Thread t = new Thread(job);
		t.start();
		pause(1000);
		t = new Thread(job);
		t.start();
		try {
		    t.join();
		}
		catch(InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    @Test
    public void cascadeKills() {// TODO more cases
	addMadkitArgs(LevelOption.agentLogLevel.toString(), "OFF");
	launchTest(new AbstractAgent() {

	    protected void activate() {
		Agent a = new NormalLife(false, true);
		assertEquals(SUCCESS, launchAgent(a, 1));
		assertNotNull(a);
		KillTargetAgent ka = new KillTargetAgent(a);
		launchAgent(ka);
		killAgent(ka);
		assertAgentIsTerminated(ka);
		assertAgentIsTerminated(a);
	    }
	});
    }

    @Test
    public void immediateKillWithTimeOut() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		Agent a = new NormalLife(false, true);
		assertEquals(SUCCESS, launchAgent(a));
		assertNotNull(a);
		assertEquals(SUCCESS, killAgent(a, 1));
		ReturnCode res = killAgent(a, 2);
		assertTrue(ALREADY_KILLED == res);
		pause(1500);
		assertAgentIsTerminated(a);
	    }
	});
    }

    @Test
    public void immediateKill() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		Agent a = new NormalLife(false, true);
		assertEquals(SUCCESS, launchAgent(a));
		pause(1000);
		assertEquals(SUCCESS, killAgent(a));
		pause(100);
		assertAgentIsTerminated(a);
		Agent b = (Agent) launchAgent(Agent.class.getName(), 10);
		killAgent(b, 0);
		pause(100);
		assertAgentIsTerminated(b);
	    }
	});
    }

    @Test
    public void randomTesting() {
	RandomT.killingOn = false;
	launchTest(new AbstractAgent() {

	    protected void activate() {
		ArrayList<AbstractAgent> agents = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
		    agents.add(new RandomT());
		}
		RandomT.agents = agents;
		assertEquals(SUCCESS, launchAgent(agents.get(0), 1));
		boolean notFinished = true;
		while (notFinished) {
		    getLogger().fine("waiting for the end of the test");
		    pause(1000);
		    notFinished = false;
		    for (AbstractAgent randomTest : agents) {
			try {
			    if (randomTest.getState() != State.TERMINATED && randomTest.getState() != State.NOT_LAUNCHED) {
				notFinished = true;
				getLogger().fine("Waiting termination of " + randomTest.getName() + " state is " + randomTest.getState());
			    }
			}
			catch(IllegalArgumentException e) {
			    e.printStackTrace();
			}
		    }
		}
	    }
	}, false);
	RandomT.killingOn = true;
	launchTest(new AbstractAgent() {

	    protected void activate() {
		ArrayList<AbstractAgent> agents = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
		    agents.add(new RandomT());
		}
		RandomT.agents = agents;
		assertEquals(SUCCESS, launchAgent(agents.get(0), 1));
		boolean notFinished = true;
		while (notFinished) {
		    getLogger().fine("waiting for the end of the test");
		    pause(1000);
		    notFinished = false;
		    for (AbstractAgent randomTest : agents) {
			try {
			    if (randomTest.getState() != State.TERMINATED && randomTest.getState() != State.NOT_LAUNCHED) {
				notFinished = true;
				getLogger().fine("Waiting termination of " + randomTest.getName() + " state is " + randomTest.getState());
			    }
			}
			catch(IllegalArgumentException e) {
			    e.printStackTrace();
			}
		    }
		}
	    }
	}, false);
    }

}

class TimeOutAgent extends Agent {

    @Override
    protected void live() {
	while (true)
	    pause(1000);
    }

    protected void end() {
    }
}

class SelfKillAgent extends DoItDuringLifeCycleAgent {

    public SelfKillAgent() {
	super();
    }

    public SelfKillAgent(boolean inActivate, boolean inLive, boolean inEnd) {
	super(inActivate, inLive, inEnd);
    }

    public SelfKillAgent(boolean inActivate, boolean inLive) {
	super(inActivate, inLive);
    }

    public SelfKillAgent(boolean inActivate) {
	super(inActivate);
    }

    @Override
    public void doIt() {
	super.doIt();
	killAgent(this);
    }

}