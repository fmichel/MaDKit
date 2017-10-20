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
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import madkit.gui.AgentFrame;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.testing.util.agent.BlockedInActivateAgent;
import madkit.testing.util.agent.BuggedConstructorAgent;
import madkit.testing.util.agent.BuggedFrameAgent;
import madkit.testing.util.agent.SelfLaunchAA;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class LaunchAbstractAgentTest extends JunitMadkit {

    final AbstractAgent target = new AbstractAgent() {

	protected void activate() {
	    assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
	    assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
	    assertEquals(ALREADY_LAUNCHED, launchAgent(this));
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

    final AbstractAgent faulty = new AbstractAgent() {

	@SuppressWarnings("null")
	protected void activate() {
	    Object o = null;
	    o.toString();
	}
    };

    @Test
    public void returnSuccessAndAlreadyNoGUI() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		assertEquals(SUCCESS, launchAgent(target));
		// assertNull(target.getGUIComponent());
		assertEquals(ALREADY_LAUNCHED, launchAgent(target));
	    }
	});
    }

    @Test
    public void launchBuggedFrameAgent() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		launchAgent(new BuggedFrameAgent(), true);
	    }
	});
    }

    @Test
    public void launchBuggedConstructorAgent() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		launchAgent(new BuggedConstructorAgent());
	    }
	}, AGENT_CRASH);
    }

    @Test
    public void nullArgs() {
	addMadkitArgs(LevelOption.agentLogLevel.toString(), "OFF");
	launchTest(new AbstractAgent() {

	    protected void activate() {
		try {
		    assertEquals(SUCCESS, launchAgent((AbstractAgent) null));
		    noExceptionFailure();
		}
		catch(NullPointerException e) {
		    throw e;
		}
	    }
	}, AGENT_CRASH);
	launchTest(new AbstractAgent() {

	    protected void activate() {
		try {
		    assertEquals(SUCCESS, launchAgent((AbstractAgent) null, true));
		    noExceptionFailure();
		}
		catch(NullPointerException e) {
		    throw e;
		}
	    }
	}, AGENT_CRASH);
	launchTest(new AbstractAgent() {

	    protected void activate() {
		try {
		    assertEquals(SUCCESS, launchAgent((AbstractAgent) null, 1));
		    noExceptionFailure();
		}
		catch(NullPointerException e) {
		    throw e;
		}
	    }
	}, AGENT_CRASH);
	launchTest(new AbstractAgent() {

	    protected void activate() {
		try {
		    assertEquals(SUCCESS, launchAgent((AbstractAgent) null, 1, true));
		    noExceptionFailure();
		}
		catch(NullPointerException e) {
		    throw e;
		}
	    }
	}, AGENT_CRASH);
    }

    @Test
    public void returnSuccessAndAlreadyLaunchWithGUI() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		assertEquals(SUCCESS, launchAgent(target, true));
		assertEquals(ALREADY_LAUNCHED, launchAgent(target, true));
		// assertNotNull(target.getGUIComponent());
	    }
	});
    }

    @Test
    public void returntTimeOut() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		assertEquals(TIMEOUT, launchAgent(timeOutAgent, 1));
		assertEquals(TIMEOUT, launchAgent(new AbstractAgent(), 0));
		assertEquals(TIMEOUT, launchAgent(new AbstractAgent(), -1));
		assertEquals(ALREADY_LAUNCHED, launchAgent(timeOutAgent));
	    }
	});
    }

    @Test
    public void returntTimeOutOnActivate() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		assertEquals(TIMEOUT, launchAgent(new BlockedInActivateAgent(), 1));
		assertEquals(TIMEOUT, launchAgent(new BlockedInActivateAgent() {

		    @Override
		    public void setupFrame(AgentFrame frame) {
			super.setupFrame(frame);
			waitNextMessage(2000);
		    }
		}, 1));
	    }
	});
    }

    @Test
    public void massLaunch() {
	addMadkitArgs(LevelOption.agentLogLevel.toString(), "OFF");
	launchTest(new AbstractAgent() {

	    int number = 1000;

	    protected void activate() {
		assertEquals(SUCCESS, createGroup(JunitMadkit.COMMUNITY, JunitMadkit.GROUP));
		startTimer();
		for (int i = 0; i < number; i++) {
		    assertEquals(SUCCESS, launchAgent(new AbstractAgent()));
		}
		stopTimer("launch time ");
		startTimer();
		for (int i = 0; i < number; i++) {
		    ReturnCode r = launchAgent(new AbstractAgent(), 0);
		    assertTrue(TIMEOUT == r || SUCCESS == r);
		}
		stopTimer("launch time ");
	    }
	});
    }

    @Test
    public void massLaunchWithGUI() {
	addMadkitArgs(LevelOption.agentLogLevel.toString(), "OFF");
	launchTest(new AbstractAgent() {

	    int number = 10;

	    protected void activate() {
		assertEquals(SUCCESS, createGroup(JunitMadkit.COMMUNITY, JunitMadkit.GROUP));
		startTimer();
		for (int i = 0; i < number; i++) {
		    assertEquals(SUCCESS, launchAgent(new AbstractAgent(), true));
		}
		stopTimer("launch time ");
		startTimer();
		for (int i = 0; i < number; i++) {
		    ReturnCode r = launchAgent(new AbstractAgent(), 0, true);
		    assertTrue(TIMEOUT == r || SUCCESS == r);
		}
		stopTimer("launch time ");
	    }
	});
    }

    @Test
    public void returnAleradyLaunch() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		assertEquals(SUCCESS, launchAgent(target));
		assertEquals(ALREADY_LAUNCHED, launchAgent(target));

		ReturnCode r = launchAgent(timeOutAgent, 0, true);
		assertTrue(TIMEOUT == r || SUCCESS == r);
		r = launchAgent(timeOutAgent, true);
		assertTrue(ALREADY_LAUNCHED == r || SUCCESS == r);
	    }
	});
    }

    @Test
    public void returnAgentCrash() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		assertEquals(AGENT_CRASH, launchAgent(faulty));
		assertEquals(ALREADY_LAUNCHED, launchAgent(faulty));
	    }
	});
    }

    @Test
    public void SelfLaunching() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		SelfLaunchAA a = new SelfLaunchAA(true);
		assertEquals(SUCCESS, launchAgent(a, 1));
		a = new SelfLaunchAA(false, true);
		assertEquals(SUCCESS, launchAgent(a, 1));
	    }
	});
    }

    @Test
    public void chainLaunching() {
	addMadkitArgs(LevelOption.agentLogLevel.toString(), "OFF");
	launchTest(new AbstractAgent() {

	    protected void activate() {
		for (int i = 0; i < 100; i++) {
		    launchAgent(new AbstractAgent() {

			protected void activate() {
			    launchAgent(new AbstractAgent() {

				protected void activate() {
				    launchAgent(new AbstractAgent());
				}
			    });
			}
		    });
		}
	    }
	});
    }
}
