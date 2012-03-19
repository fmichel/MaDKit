/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.api.abstractAgent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.AbstractAgent.State;
import madkit.kernel.JunitMadKit;
import madkit.testing.util.agent.FaultyAA;
import madkit.testing.util.agent.SimulatedAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.14
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class LaunchAgentBucketWithRolesWithListTest extends JunitMadKit {

	static int	size	= 1001;

	@Test
	public void cannotLaunch() {
		launchTest(new AbstractAgent() {

			protected void activate() {
				launchAgentBucketWithRoles(FaultyAA.class.getName(),size, COMMUNITY + ";" + GROUP + ";" + ROLE);
			}
		});
	}

	@Test
	public void nullArg() {
		launchTest(new AbstractAgent() {

			protected void activate() {
				List<AbstractAgent> l = new ArrayList<AbstractAgent>();
				for (int i = 0; i < 1; i++) {
					l.add(null);
				}
				launchAgentBucketWithRoles(l, COMMUNITY + ";" + GROUP + ";" + ROLE);
			}
		});
	}

	@Test
	public void wrongCGR() {
		launchTest(new AbstractAgent() {

			protected void activate() {
				try {
					launchAgentBucketWithRoles(FaultyAA.class.getName(),size, COMMUNITY + ";" + GROUP + ";" + ROLE);
					JunitMadKit.noExceptionFailure();
				} catch (IllegalArgumentException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void returnSuccess() {
		launchTest(new AbstractAgent() {

			protected void activate() {
				List<AbstractAgent> l = new ArrayList<AbstractAgent>();
				for (int i = 0; i < size; i++) {
					l.add(new SimulatedAgent());
				}
				launchAgentBucketWithRoles(l, COMMUNITY + ";" + GROUP + ";" + ROLE);
				testAgents(l);
				assertEquals(size, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
			}
		});
	}

	private void testAgents(List<AbstractAgent> l) {
		for (AbstractAgent abstractAgent : l) {
			assertTrue(abstractAgent.isAlive());
			assertEquals(State.ACTIVATED, abstractAgent.getState());
			assertTrue(((SimulatedAgent) abstractAgent).goneThroughActivate());
		}
	}

}
