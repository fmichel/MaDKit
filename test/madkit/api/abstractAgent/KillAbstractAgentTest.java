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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.logging.Level;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.testing.util.agent.DoItDuringLifeCycleAbstractAgent;
import madkit.testing.util.agent.FaultyAA;
import madkit.testing.util.agent.NormalAA;
import madkit.testing.util.agent.SelfAbstractKill;
import madkit.testing.util.agent.TimeOutAA;
import madkit.testing.util.agent.UnstopableAbstractAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class KillAbstractAgentTest extends JunitMadkit {

	@Test
	public void returnSuccess() {
		addMadkitArgs(LevelOption.kernelLogLevel.toString(), "FINEST");
		launchTest(new AbstractAgent() {
			protected void activate() {
//				setLogLevel(Level.ALL);
				NormalAA naa = new NormalAA();
				assertEquals(SUCCESS, launchAgent(naa));
				assertEquals(SUCCESS, killAgent(naa));
			}
		});
	}

	@Test
	public void returnNOT_YET_LAUNCHEDAfterImmediateLaunch() {
		launchTest(new AbstractAgent() {
			protected void activate() {
//				setLogLevel(Level.ALL);
				TimeOutAA to = new TimeOutAA(true, true);
				assertEquals(TIMEOUT, launchAgent(to, 0));
				ReturnCode r = killAgent(to);
				assertTrue(r == NOT_YET_LAUNCHED || r == SUCCESS);
			}
		}, true);
	}

	@Test
	public void returnAlreadyKilledAfterCrash() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), "FINEST");
		launchTest(new AbstractAgent() {
			protected void activate() {
//				setLogLevel(Level.ALL);
				FaultyAA f = new FaultyAA(true);
				if (logger != null)
					logger.info("activating");
				assertEquals(AGENT_CRASH, launchAgent(f));
				pause(100);
				assertEquals(ALREADY_KILLED, killAgent(f));
			}
		});
	}

	@Test
	public void returnAlreadyKilled() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), "FINEST");
		launchTest(new AbstractAgent() {
			protected void activate() {
//				setLogLevel(Level.ALL);
				AbstractAgent f = new AbstractAgent();
				if (logger != null)
					logger.info("activating");
				assertEquals(SUCCESS, launchAgent(f));
				assertEquals(SUCCESS, killAgent(f));
				assertEquals(ALREADY_KILLED, killAgent(f));
				assertAgentIsTerminated(f);
			}
		});
	}

	@Test
	public void massKill() {
//		addMadkitArgs(LevelOption.agentLogLevel.toString(), "FINEST");
//		addMadkitArgs(LevelOption.kernelLogLevel.toString(), "ALL");
		launchTest(new AbstractAgent() {
			ArrayList<AbstractAgent> list = new ArrayList<>(100);

			protected void activate() {
//				setLogLevel(Level.ALL);
				startTimer();
				for (int i = 0; i < 100; i++) {
					AbstractAgent t = new AbstractAgent();
					list.add(t);
					assertEquals(SUCCESS, launchAgent(t));
				}
				stopTimer("launch time ");
				startTimer();
				for (AbstractAgent a : list) {
					// killAgent(a,0);
					assertEquals(SUCCESS, killAgent(a, 1));
				}
				stopTimer("kill time ");
			}
		});
	}

	@Test
	public void returnTimeOut() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				setLogLevel(Level.ALL);
				TimeOutAA to = new TimeOutAA(true, true);
				assertEquals(TIMEOUT, launchAgent(to, 1));
				while (to.getState() != State.LIVING) {
					pause(100);
				}
				assertEquals(TIMEOUT, killAgent(to, 1));
				assertEquals(ALREADY_KILLED, killAgent(to));
				assertEquals(ALREADY_LAUNCHED, launchAgent(to));
				pause(1000);
			}
		}, true);
	}

	@Test
	public void returnAleradyKilled() {
		addMadkitArgs(LevelOption.kernelLogLevel.toString(), "ALL");
		launchTest(new AbstractAgent() {
			protected void activate() {
				NormalAA target = new NormalAA();
				setLogLevel(Level.ALL);
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(SUCCESS, killAgent(target));
				assertEquals(ALREADY_KILLED, killAgent(target));
			}
		});
	}

	@Test
	public void noTimeoutKill() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), "ALL");
		launchTest(new AbstractAgent() {
			protected void activate() {
				setLogLevel(Level.ALL);
				NormalAA target = new NormalAA();
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(SUCCESS, killAgent(target, 0));
			}
		});
	}

	@Test
	public void returnAgentCrash() {
		addMadkitArgs(LevelOption.kernelLogLevel.toString(), "ALL");
		launchTest(new AbstractAgent() {
			protected void activate() {
				setLogLevel(Level.ALL);
				FaultyAA f = new FaultyAA(true);
				assertEquals(AGENT_CRASH, launchAgent(f));
				assertEquals(ALREADY_LAUNCHED, launchAgent(f));
			}
		});
	}

	@Test
	public void selfKill() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				setLogLevel(Level.OFF);

				ReturnCode r = launchAgent(new SelfKillAA(true), 1);
				assertTrue(r == SUCCESS || r == AGENT_CRASH);
				AbstractAgent a = new SelfKillAA(false, true);
				assertEquals(SUCCESS, launchAgent(a, 1));
				assertEquals(SUCCESS, killAgent(a, 1));
			}
		});
	}

	@Test
	public void selfKillInActivate() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				setLogLevel(Level.OFF);
				assertEquals(SUCCESS, launchAgent(new SelfKillAA(true, true)));
				assertEquals(SUCCESS, launchAgent(new SelfKillAA(true, false)));
			}
		});
	}

	@Test
	public void selfKillInActivateWTO() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				setLogLevel(Level.OFF);
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(true, false, 0)));
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(true, false, 1)));
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(true, true, 1)));
			}
		});
	}

	@Test
	public void selfKilling() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				// the time out should not change anything because target == this
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(true, false, 0)));
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(true, false, 1)));
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(true, false, Integer.MAX_VALUE)));
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(true, true, 0)));
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(true, true, 1)));
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(true, true, Integer.MAX_VALUE)));
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(false, true, 0)));
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(false, true, 1)));
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(false, true, Integer.MAX_VALUE)));
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(false, false, 0)));
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(false, false, 1)));
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(false, false, Integer.MAX_VALUE)));
			}
		});
	}

	@Test
	public void killFaulty() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent a;
				assertEquals(AGENT_CRASH, launchAgent(a = new FaultyAA(true, false)));
				ReturnCode r = killAgent(a);
				assertTrue(r == SUCCESS || r == ALREADY_KILLED);

				assertEquals(TIMEOUT, launchAgent(a = new FaultyAA(true, false), 0));
				pause(10);
				r = killAgent(a);
				assertTrue(r == SUCCESS || r == ALREADY_KILLED);

				assertEquals(TIMEOUT, launchAgent(a = new FaultyAA(true, false), 0));
				pause(200);
				assertEquals(ALREADY_KILLED, killAgent(a));

				// in end
				assertEquals(SUCCESS, launchAgent(a = new FaultyAA(false, true)));
				assertEquals(SUCCESS, killAgent(a));

				assertEquals(SUCCESS, launchAgent(a = new FaultyAA(false, true)));
				assertEquals(SUCCESS, killAgent(a));

				assertEquals(SUCCESS, launchAgent(a = new FaultyAA(false, true)));
				assertEquals(SUCCESS, killAgent(a));

				assertEquals(SUCCESS, launchAgent(a = new FaultyAA(false, true)));
				assertEquals(SUCCESS, killAgent(a, 0));
				pause(10);// avoid interleaving
				assertEquals(ALREADY_KILLED, killAgent(a));

			}
		});
	}

	@Test
	public void selfKillInActivateAndEnd() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				SelfAbstractKill a = new SelfAbstractKill(true, true, 0);
				assertEquals(SUCCESS, launchAgent(a));
				assertAgentIsTerminated(a);
			}
		});
	}

	@Test
	public void selfKillinActivateWithTimeout() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				SelfAbstractKill a = new SelfAbstractKill(true, false, 1);
				assertEquals(SUCCESS, launchAgent(a));
				assertEquals(ALREADY_KILLED, killAgent(a));
				assertAgentIsTerminated(a);
			}
		});
	}

	@Test
	public void selfKillinEndAndWaitKill() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(new SelfAbstractKill(false, true, Integer.MAX_VALUE)));
			}
		});
	}

	@Test
	public void cascadeKills() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				Killer a = new Killer();
				Killer b = new Killer();
				a.setTarget(b);
				b.setTarget(a);
				launchAgent(a);
				launchAgent(b);
				assertEquals(SUCCESS, killAgent(a));
				assertEquals(ALREADY_KILLED, killAgent(a));
			}
		});
	}

	@Test
	public void returnSuccessAfterLaunchTimeOut() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				TimeOutAA to = new TimeOutAA(true, true);
				assertEquals(TIMEOUT, launchAgent(to, 1));
				assertEquals(SUCCESS, killAgent(to));
				assertAgentIsTerminated(to);
			}
		});
	}

	@Test
	public void killUnstopable() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAbstractAgent();
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
	public void brutalKillUnstopable() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAbstractAgent();
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				if (logger != null)
					logger.info(unstopableAgent.getState().toString());
				assertEquals(SUCCESS, killAgent(unstopableAgent, 0));
				assertAgentIsTerminated(unstopableAgent);
			}
		});
	}

	@Test
	public void brutalKillUnstopableUsingSelfRef() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAbstractAgent();
				ReturnCode r = launchAgent(unstopableAgent, 2);
				assertTrue(TIMEOUT == r || SUCCESS == r );
				assertEquals(SUCCESS, unstopableAgent.killAgent(unstopableAgent, 0));
				assertAgentIsTerminated(unstopableAgent);
			}
		});
	}

	@Test
	public void killUnstopableUsingSelfRef() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAbstractAgent();
				ReturnCode r = launchAgent(unstopableAgent, 2);
				assertTrue(TIMEOUT == r || SUCCESS == r );
				r = unstopableAgent.killAgent(unstopableAgent, 2);
				assertTrue(TIMEOUT == r || SUCCESS == r );
				assertAgentIsTerminated(unstopableAgent);
			}
		});
		// printAllStacks();
	}

}


class SelfKillAA extends DoItDuringLifeCycleAbstractAgent {

	public SelfKillAA() {
		super();
	}

	public SelfKillAA(boolean inActivate, boolean inEnd) {
		super(inActivate, inEnd);
	}

	public SelfKillAA(boolean inActivate) {
		super(inActivate);
	}

	@Override
	public void doIt() {
		super.doIt();
		killAgent(this);
	}
}


class Killer extends AbstractAgent {
	AbstractAgent target;

	/**
	 * @param target
	 *           the target to set
	 */
	final void setTarget(AbstractAgent target) {
		this.target = target;
	}

	@Override
	protected void end() {
		killAgent(target);
		killAgent(target);
	}

}
