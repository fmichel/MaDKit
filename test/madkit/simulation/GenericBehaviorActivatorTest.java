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
package madkit.simulation;

import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Scheduler;
import madkit.simulation.activator.GenericBehaviorActivator;
import madkit.testing.util.agent.FaultyAA;
import madkit.testing.util.agent.NormalAA;

import org.junit.Before;
import org.junit.Test;

/**
 * <code>null</code> in the constructor is 
 * covered by {@link BasicSchedulerTest#addingNullActivator()}
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.15
 * @version 0.9
 * 
 */

public class GenericBehaviorActivatorTest extends JunitMadkit {

	private static GenericBehaviorActivator<AbstractAgent> buggy;

	@Before
	public void setUp() throws Exception {
		buggy = new GenericBehaviorActivator<>(COMMUNITY, GROUP, ROLE, "doIt");
	}

	@Test
	public void noSuchMethodCrash() {
		launchTest(new Scheduler() {
			protected void activate() {
				launchAgent(new NormalAA());
				addActivator(buggy);
				try {
					buggy.execute();
					noExceptionFailure();
				} catch (SimulationException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void invocationException() {
		launchTest(new Scheduler() {
			protected void activate() {
				launchAgent(new FaultyAA(){
					@Override
					public void activate() {
						createDefaultCGR(this);
					}
				});
				addActivator(buggy);
				try {
					buggy.execute();
					noExceptionFailure();
				} catch (SimulationException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

}
