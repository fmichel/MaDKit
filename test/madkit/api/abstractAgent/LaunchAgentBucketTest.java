/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.api.abstractAgent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.logging.Level;

import madkit.agr.Organization;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.AbstractAgent.State;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.testing.util.agent.SimulatedAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class LaunchAgentBucketTest extends JunitMadkit {
	
	static int size = 1001;

	@Test
	public void returnSuccess() {
		for (int i = 0; i < 100; i++) {
//			addMadkitArgs(LevelOption.kernelLogLevel.toString(),Level.ALL.toString());
			launchTest(new AbstractAgent() {
				protected void activate() {
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
			addMadkitArgs(LevelOption.agentLogLevel.toString(),Level.ALL.toString());
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
			addMadkitArgs(LevelOption.agentLogLevel.toString(),Level.ALL.toString());
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
						launchAgent(new Agent(){
							@Override
							protected void activate() {
								setLogLevel(Level.ALL);
								pause(100);
								requestRole(COMMUNITY, GROUP, ROLE);
							}
							@Override
							protected void live() {
								pause(100000);
							}
						},true);
					}
				});
				t.start();
				System.err.println("begin");
				startTimer();
				launchAgentBucket(AbstractAgent.class.getName(), 1000000,COMMUNITY+","+GROUP+","+ROLE);
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				stopTimer("bucket launch time = ");
				System.err.println("done\n\n");
				requestRole(COMMUNITY, GROUP, ROLE);
				setLogLevel(Level.OFF);
				assertEquals(1000002, getAgentsWithRole(COMMUNITY, GROUP, ROLE,true).size());
			}
		});
	}


	@Test
	public void nullArg() {
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),Level.ALL.toString());
		launchTest(new AbstractAgent() {
			protected void activate() {
				try {
					launchAgentBucket((String)null, size);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}

		},ReturnCode.AGENT_CRASH);
	}
	
	@Test
	public void classNotExist() {
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),Level.ALL.toString());
		launchTest(new AbstractAgent() {
			protected void activate() {
				launchAgentBucket("fake.fake", 2);
			}

		});
	}
	
	

	private void testAgents(List<AbstractAgent> l) {
		for (AbstractAgent abstractAgent : l) {
			assertTrue(abstractAgent.isAlive());
			assertEquals(State.ACTIVATED,abstractAgent.getState());
			assertTrue(((SimulatedAgent) abstractAgent).goneThroughActivate());
		}
	}

	@Test
	public void returnSuccessOn0() {
		launchTest(new AbstractAgent() {
			protected void activate() {
//				setLogLevel(Level.ALL);
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
				List<AbstractAgent> l = launchAgentBucket(SimulatedAgent.class.getName(),size, COMMUNITY+","+GROUP+","+ROLE);
				assertEquals(size, l.size());
				assertEquals(size, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
				//I am the manager
				assertNull(getAgentsWithRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
				assertNotNull(getAgentsWithRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE,true));
				testAgents(l);
			}
		});
	}

	
}
