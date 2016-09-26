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
package madkit.scenari.kill;

import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_KILLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.logging.Level;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.JUnitAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.testing.util.agent.UnstopableAgent;
import madkit.testing.util.agent.WorkingAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */

public class KillingWorkingThreadedAgentsTest extends JunitMadkit {

	@Test
	public void killUnstopable() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAgent();
				unstopableAgent.setLogLevel(Level.FINER);
				startTimer();
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				stopTimer("launch time out ");
				assertEquals(TIMEOUT, killAgent(unstopableAgent, 1));
				assertAgentIsTerminated(unstopableAgent);
			}
		});
	}

	@Test
	public void brutalKills() {// TODO brutal kill with to < 0
		addMadkitArgs(LevelOption.agentLogLevel.toString(), "ALL");
		addMadkitArgs(LevelOption.kernelLogLevel.toString(), "FINEST");
		launchTest(new AbstractAgent() {
			public void activate() {
				Agent a;
				a = new WorkingAgent(true, false, false);
				ReturnCode r = launchAgent(a, 1);
				assertTrue(TIMEOUT == r || r == SUCCESS);
				killAgent(a, 1);
				assertAgentIsTerminated(a);

				a = new WorkingAgent(false, true, false);
				assertEquals(SUCCESS, launchAgent(a));
				pause(100);
				r = killAgent(a, 0);
				assertTrue(ALREADY_KILLED == r || r == SUCCESS);
				assertAgentIsTerminated(a);

				a = new WorkingAgent(false, false, true);
				assertEquals(SUCCESS, launchAgent(a));
				pause(100);
				r = killAgent(a, 0);
				assertTrue(ALREADY_KILLED == r || r == SUCCESS);
				assertAgentIsTerminated(a);

				a = new WorkingAgent(true, false, true);
				launchAgent(a, 1);
				r = killAgent(a, 0);
				assertTrue(ALREADY_KILLED == r || r == SUCCESS);
				assertAgentIsTerminated(a);
			}
		});
	}

	@Test
	public void brutalKillOnEnd() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), "ALL");
		addMadkitArgs(LevelOption.kernelLogLevel.toString(), "FINEST");
		launchTest(new AbstractAgent() {
			public void activate() {
				Agent a;
				a = new WorkingAgent(false, false, true);
				assertEquals(SUCCESS, launchAgent(a));
				killAgent(a, 0);
				assertAgentIsTerminated(a);
			}
		});
	}

	@Test
	public void brutalKillOnActivate() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), "ALL");
		addMadkitArgs(LevelOption.kernelLogLevel.toString(), "FINEST");
		launchTest(new AbstractAgent() {
			public void activate() {
				Agent a;
				a = new WorkingAgent(true, false, false);
				ReturnCode r = launchAgent(a, 1);
				assertTrue(TIMEOUT == r || r == SUCCESS);
				killAgent(a, 1);
				assertAgentIsTerminated(a);
				a = new WorkingAgent(true, true, false);
				r = launchAgent(a, 1);
				assertTrue(TIMEOUT == r || r == SUCCESS);
				killAgent(a, 1);
				assertAgentIsTerminated(a);
			}
		});
	}

	@Test
	public void brutalKillOnLive() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), "ALL");
		addMadkitArgs(LevelOption.kernelLogLevel.toString(), "FINEST");
		launchTest(new JUnitAgent() {
			public void activate() {
				Agent a;
				a = new WorkingAgent(false, true, false);
				successOnLaunch(a);
				pause(10);
				killAgent(a, 0);
				assertAgentIsTerminated(a);
			}
		});
	}

	@Test
	public void brutalKillOnLiveWTO() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), "ALL");
		addMadkitArgs(LevelOption.kernelLogLevel.toString(), "FINEST");
		launchTest(new JUnitAgent() {
			public void activate() {
				Agent a;
				a = new WorkingAgent(false, true, true);
				successOnLaunch(a);
				pause(10);
				killAgent(a, 1);
				assertAgentIsTerminated(a);
				a = new WorkingAgent(false, true, false);
				successOnLaunch(a);
				pause(10);
				killAgent(a, 0);
				assertAgentIsTerminated(a);
			}
		});
	}

	@Test
	public void brutalKillonAll() {// TODO brutal kill with to < 0
		launchTest(new AbstractAgent() {
			public void activate() {
				Agent a;
				a = new WorkingAgent(true, true, true);
				launchAgent(a, 1);
				ReturnCode r = killAgent(a, 0);
				assertTrue(TIMEOUT == r || r == SUCCESS || r == ALREADY_KILLED);
				assertAgentIsTerminated(a);
			}
		}, true);
	}

	@Test
	public void brutalKillonAllWTO() {
		launchTest(new AbstractAgent() {
			public void activate() {
				Agent a;
				a = new WorkingAgent(true, true, true);
				ReturnCode r = launchAgent(a, 1);
				assertTrue(TIMEOUT == r || r == SUCCESS);
				r = killAgent(a, 1);
				assertTrue(TIMEOUT == r || r == SUCCESS || r == ALREADY_KILLED);
				assertAgentIsTerminated(a);
			}
		}, true);
	}

	@Test
	public void normalKills() {// TODO more cases
		launchTest(new AbstractAgent() {
			public void activate() {
				super.activate();
				Agent a;
				a = new WorkingAgent(true, false, false);
				ReturnCode r = launchAgent(a, 1);
				assertTrue(TIMEOUT == r || r == SUCCESS);
				killAgent(a);
				assertAgentIsTerminated(a);

				a = new WorkingAgent(false, true, false);
				assertEquals(SUCCESS, launchAgent(a));
				killAgent(a);
				assertAgentIsTerminated(a);

				a = new WorkingAgent(false, false, true);
				assertEquals(SUCCESS, launchAgent(a));
				pause(100);
				killAgent(a, 1);
				assertEquals(State.TERMINATED, a.getState());

				a = new WorkingAgent(true, false, true);
				r = launchAgent(a, 1);
				assertTrue(TIMEOUT == r || r == SUCCESS);
				killAgent(a, 1);
				assertEquals(State.TERMINATED, a.getState());

				a = new WorkingAgent(true, true, true);
				r = launchAgent(a, 1);
				assertTrue(TIMEOUT == r || r == SUCCESS);
				killAgent(a, 1);
				assertEquals(State.TERMINATED, a.getState());
			}
		});
	}
}
