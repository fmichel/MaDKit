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
package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static org.junit.Assert.assertEquals;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.testing.util.agent.SelfLaunch;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.5
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class LaunchAgentTest extends JunitMadkit {

	final AbstractAgent target = new Agent() {
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
			assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
			assertEquals(ALREADY_LAUNCHED, launchAgent(this));
		}
	};

	final AbstractAgent timeOutAgent = new Agent() {
		protected void activate() {
			pause(2000);
		}
	};

	final AbstractAgent faulty = new Agent() {
		@SuppressWarnings("null")
		protected void activate() {
			Object o = null;
			o.toString();
		}
	};

	@Test
	public void returnSuccessAndAlreadyLaunch() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(ALREADY_LAUNCHED, launchAgent(target));
				assertEquals(ALREADY_LAUNCHED, launchAgent(this));
			}
		});
	}

	@Test
	public void returnTimeOut() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(TIMEOUT, launchAgent(timeOutAgent, 1));
				assertEquals(ALREADY_LAUNCHED, launchAgent(timeOutAgent));
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
	public void killLauncher() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent a;
				launchAgent(a = new AbstractAgent() {
					@Override
					protected void activate() {
						assertEquals(TIMEOUT, launchAgent(timeOutAgent, 1));
					}
				}, 1);
				killAgent(a);
				assertAgentIsTerminated(a);
			}
		}, true);
		pause(1000);
	}

	@Test
	public void returnAgentCrash() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(AGENT_CRASH, launchAgent(faulty, 1));
				assertEquals(ALREADY_LAUNCHED, launchAgent(faulty));
			}
		});
	}

	@Test
	public void selfLaunching() {
		addMadkitArgs("--kernelLogLevel", "ALL");
		launchTest(new AbstractAgent() {
			protected void activate() {
				SelfLaunch a = new SelfLaunch(true);
				assertEquals(SUCCESS, launchAgent(a, 1));
				a = new SelfLaunch(false, true);
				assertEquals(SUCCESS, launchAgent(a, 1));
				a = new SelfLaunch(false, false, true);
				assertEquals(SUCCESS, launchAgent(a, 1));
			}
		});
	}

	@Test
	public void nullArgs() {
		addMadkitArgs("--kernelLogLevel", "ALL");
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(TIMEOUT, launchAgent(new AbstractAgent(), -1, true));
			}
		});
	}

}
