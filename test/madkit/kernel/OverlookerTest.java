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
package madkit.kernel;

import static org.junit.Assert.assertEquals;
import madkit.kernel.Madkit.LevelOption;
import madkit.simulation.EmptyActivator;
import madkit.testing.util.agent.NormalAgent;
import madkit.testing.util.agent.SimulatedAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.15
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class OverlookerTest extends JunitMadkit {

	@Test
	public void buggyActivator() {
		String[] myArgs = { LevelOption.kernelLogLevel.toString(), "ALL" };
		addMadkitArgs(myArgs);
		launchTest(new Scheduler() {

			@Override
			protected void activate() {
				addActivator(new EmptyActivator(COMMUNITY, GROUP, ROLE) {
					@SuppressWarnings("null")
					@Override
					protected void adding(AbstractAgent theAgent) {
						Object o = null;
						o.toString();
					}
				});
				assertEquals(ReturnCode.AGENT_CRASH,
						launchAgent(new SimulatedAgent()));
			}
		});
	}

	@Test
	public void addingTest() {
		String[] myArgs = { LevelOption.kernelLogLevel.toString(), "ALL" };
		addMadkitArgs(myArgs);

		launchTest(new Scheduler() {

			@Override
			protected void activate() {
				testFails(new Exception());
				addActivator(new EmptyActivator(COMMUNITY, GROUP, ROLE) {

					@Override
					protected void adding(AbstractAgent theAgent) {
						testFailed = false;
					}
				});
				assertEquals(ReturnCode.SUCCESS, launchAgent(new SimulatedAgent()));
			}
		});
	}

	@Test
	public void removingTest() {
		addMadkitArgs(LevelOption.kernelLogLevel.toString(), "ALL",
				LevelOption.agentLogLevel.toString(), "ALL");

		launchTest(new Scheduler() {

			@Override
			protected void activate() {
				testFails(new Exception());
				addActivator(new EmptyActivator(COMMUNITY, GROUP, ROLE) {
					protected void removing(AbstractAgent theAgent) {
						if (logger != null)
							logger.info("\nremoving OK " + theAgent);
						testFailed = false;
					}
				});
				NormalAgent a;
				assertEquals(ReturnCode.SUCCESS, launchAgent(a = new NormalAgent()));
				pause(10);
			}
		});
	}

}
