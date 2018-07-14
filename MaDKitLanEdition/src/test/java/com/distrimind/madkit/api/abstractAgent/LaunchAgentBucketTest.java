/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.api.abstractAgent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.junit.Assert;
import org.junit.Test;

import com.distrimind.madkit.agr.Organization;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.Agent;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.AbstractAgent.ReturnCode;
import com.distrimind.madkit.kernel.AbstractAgent.State;
import com.distrimind.madkit.kernel.Role;
import com.distrimind.madkit.testing.util.agent.SimulatedAgent;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.14
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */

public class LaunchAgentBucketTest extends JunitMadkit {

	static final int size = 1001;

	@Test
	public void returnSuccess() {
		for (int i = 0; i < 100; i++) {
			// addMadkitArgs(LevelOption.kernelLogLevel.toString(),Level.ALL.toString());
			launchTest(new AbstractAgent() {
				@Override
				protected void activate() {
					List<AbstractAgent> l = launchAgentBucket(SimulatedAgent.class.getName(), size);
					assertEquals(size, l.size());
					testAgents(l);
					assertEquals(size, getAgentsWithRole(GROUP, ROLE).size());
				}

			});
		}
	}

	@Test
	public void withAnAgentClass() {
		addMadkitArgs("--agentLogLevel", Level.ALL.toString());
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				List<AbstractAgent> l = launchAgentBucket(Agent.class.getName(), size);
				assertNotNull(l);
				killAgent(l.get(0));
			}
		});
	}

	@Test
	public void withAnAAClass() {
		addMadkitArgs("--agentLogLevel", Level.ALL.toString());
		launchTest(new AbstractAgent() {
			@Override
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
			@Override
			protected void activate() {
				createGroup(GROUP);
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						launchAgent(new Agent() {
							@Override
							protected void activate() throws InterruptedException {
								setLogLevel(Level.ALL);
								pause(100);
								requestRole(GROUP, ROLE);
							}

							@Override
							protected void liveCycle() throws InterruptedException {
								pause(10000);
								this.killAgent(this);

							}
						}, true);
					}
				});
				t.start();
				System.err.println("begin");
				startTimer();
				launchAgentBucket(AbstractAgent.class.getName(), 1000000, new Role(GROUP, ROLE));
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				stopTimer("bucket launch time = ");
				System.err.println("done\n\n");
				requestRole(GROUP, ROLE);
				setLogLevel(Level.OFF);
				assertEquals(1000002, getAgentsWithRole(GROUP, ROLE, true).size());
			}
		});
	}

	@Test
	public void nullArg() {
		addMadkitArgs("--kernelLogLevel", Level.ALL.toString());
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchAgentBucket((String) null, size);
				noExceptionFailure();
			}

		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void classNotExist() {
		addMadkitArgs("--kernelLogLevel", Level.ALL.toString());
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchAgentBucket("fake.fake", 2);
			}

		});
	}

	protected void testAgents(List<AbstractAgent> l) {
		for (AbstractAgent abstractAgent : l) {
			assertTrue(abstractAgent.isAlive());
			assertEquals(State.ACTIVATED, abstractAgent.getState());
			assertTrue(((SimulatedAgent) abstractAgent).goneThroughActivate());
		}
	}

	@Test
	public void returnSuccessOn0() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				// setLogLevel(Level.ALL);
				List<AbstractAgent> l = launchAgentBucket(SimulatedAgent.class.getName(), 0);
				assertEquals(0, l.size());
				assertEquals(0, getAgentsWithRole(GROUP, ROLE).size());
				testAgents(l);
			}
		});
	}

	@Test
	public void returnSuccessOn1() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				List<AbstractAgent> l = launchAgentBucket(SimulatedAgent.class.getName(), 1);
				assertEquals(1, l.size());
				assertEquals(1, getAgentsWithRole(GROUP, ROLE).size());
				testAgents(l);
			}
		});
	}

	@Test
	public void moreCPUThanAgents() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				int nbOfAgents = 50;
				for (int j = 0; j < nbOfAgents; j++) {
					for (int i = 1; i < Runtime.getRuntime().availableProcessors() * 2; i++) {
						List<AbstractAgent> l = launchAgentBucket(SimulatedAgent.class.getName(), j, i);
						assertEquals(j, l.size());
						final Set<AgentAddress> agentsWithRole = getAgentsWithRole(GROUP, ROLE);
						if (agentsWithRole != null) {
							assertEquals(j, agentsWithRole.size());
						}
						testAgents(l);
						destroyGroup(GROUP);
					}
				}
			}
		});
	}

	@Test
	public void returnSuccessWithName() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				List<AbstractAgent> l = launchAgentBucket(SimulatedAgent.class.getName(), size, new Role(GROUP, ROLE));
				assertEquals(size, l.size());
				assertEquals(size, getAgentsWithRole(GROUP, ROLE).size());
				// I am the manager
				assertEquals(0, getAgentsWithRole(GROUP, Organization.GROUP_MANAGER_ROLE).size());
				Assert.assertNotEquals(0, getAgentsWithRole(GROUP, Organization.GROUP_MANAGER_ROLE, true).size());
				testAgents(l);
			}
		});
	}

}
