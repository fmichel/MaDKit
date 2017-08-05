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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.Test;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.Scheduler;
import com.distrimind.madkit.kernel.AbstractAgent.ReturnCode;
import com.distrimind.madkit.kernel.AbstractAgent.State;
import com.distrimind.madkit.kernel.Role;
import com.distrimind.madkit.simulation.SimulationException;
import com.distrimind.madkit.simulation.activator.GenericBehaviorActivator;
import com.distrimind.madkit.testing.util.agent.FaultyAA;
import com.distrimind.madkit.testing.util.agent.SimulatedAgent;
import com.distrimind.madkit.testing.util.agent.SimulatedAgentThatLaunchesASimulatedAgent;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.14
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */

public class LaunchAgentBucketWithRolesWithListTest extends JunitMadkit {

	GenericBehaviorActivator<AbstractAgent> buggy;
	static int size = 1001;

	@Before
	public void setUp() throws Exception {
		buggy = new GenericBehaviorActivator<>(GROUP, ROLE, "doIt");
	}

	@Test
	public void cannotLaunch() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchAgentBucket(FaultyAA.class.getName(), size, new Role(GROUP, ROLE));
			}
		});
	}

	@Test
	public void nullArg() {
		addMadkitArgs("--kernelLogLevel", Level.ALL.toString());
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				List<AbstractAgent> l = new ArrayList<>();
				for (int i = 0; i < 1; i++) {
					l.add(null);
				}
				launchAgentBucket(l, new Role(GROUP, ROLE));
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void wrongCGR() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				try {
					launchAgentBucket(FaultyAA.class.getName(), size, new Role(GROUP, ROLE));
					JunitMadkit.noExceptionFailure();
				} catch (IllegalArgumentException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void returnSuccess() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				List<SimulatedAgent> l = new ArrayList<>();
				for (int i = 0; i < size; i++) {
					l.add(new SimulatedAgent());
				}
				launchAgentBucket(l, new Role(GROUP, ROLE));
				testAgents(l);
				assertEquals(size, getAgentsWithRole(GROUP, ROLE).size());
			}
		});
	}

	@Test
	public void returnSuccessWithInsideLaunches() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				List<SimulatedAgent> l = new ArrayList<>();
				for (int i = 0; i < 2; i++) {
					l.add(new SimulatedAgentThatLaunchesASimulatedAgent());
				}
				launchAgentBucket(l, new Role(GROUP, ROLE));
				testAgents(l);
				assertEquals(4, getAgentsWithRole(GROUP, ROLE).size());
			}
		});
	}

	@Test
	public void testBucketRequestRole() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				List<AbstractAgent> l = new ArrayList<>();
				for (int i = 0; i < 5; i++) {
					l.add(new AbstractAgent() {
						@Override
						protected void activate() {
							assertEquals(ReturnCode.IGNORED, requestRole(GROUP, ROLE2, null));
							bucketModeCreateGroup(GROUP2, null);
							assertEquals(ReturnCode.SUCCESS, bucketModeRequestRole(GROUP2, ROLE2, null));
						}
					});
				}
				launchAgentBucket(l, new Role(GROUP, ROLE));
				testAgentsRoles(l);
				assertEquals(5, getAgentsWithRole(GROUP, ROLE).size());
			}
		});
	}

	@Test
	public void inScheduledAgent() {
		launchTest(new Scheduler() {
			@Override
			protected void activate() {
				GenericBehaviorActivator<AbstractAgent> test = new GenericBehaviorActivator<>(GROUP, ROLE,
						"launchAgentBucketWithRoles");
				launchAgent(new SimulatedAgent());
				addActivator(test);
				try {
					test.execute();
				} catch (SimulationException e) {
					e.printStackTrace();
					throw e;
				}
				this.killAgent(this);
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
			abstractAgent.setLogLevel(Level.ALL);
			assertTrue(abstractAgent.isAlive());
			ReturnCode requestRole = abstractAgent.requestRole(GROUP, ROLE);
			System.err.println(requestRole);
			assertEquals(ReturnCode.ROLE_ALREADY_HANDLED, requestRole);
			requestRole = abstractAgent.requestRole(GROUP, ROLE2);
			System.err.println(requestRole);
			assertEquals(ReturnCode.SUCCESS, requestRole);
			requestRole = abstractAgent.requestRole(GROUP2, ROLE2);
			System.err.println(requestRole);
			assertEquals(ReturnCode.ROLE_ALREADY_HANDLED, requestRole);
		}
	}

}
