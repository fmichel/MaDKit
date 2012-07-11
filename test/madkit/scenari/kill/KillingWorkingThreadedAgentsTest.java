/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
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
@SuppressWarnings("serial")
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
				assertEquals(SUCCESS, killAgent(a, 0));
				assertAgentIsTerminated(a);
				pause(1000);
			}
		});
	}

	@Test
	public void brutalKillOnEnd() {// TODO brutal kill with to < 0
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
	public void brutalKillOnActivate() {// TODO brutal kill with to < 0
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
	public void brutalKillOnLive() {// TODO brutal kill with to < 0
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
	public void brutalKillOnLiveWTO() {// TODO brutal kill with to < 0
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
	public void brutalKillonAllWTO() {// TODO brutal kill with to < 0
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
				// TODO Auto-generated method stub
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
