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
package madkit.performance;

import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.testing.util.agent.NormalAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.16
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class MemoryLeak extends JunitMadkit {

//	@Test
	public void massAALaunchWithBucket() {// TODO more cases
//		addMadkitArgs(LevelOption.agentLogLevel.toString(),Level.OFF.toString());
		launchTest(new NormalAgent() {
			protected void activate() {
				pause(2000);
				while (true) {
					for (int i = 0; i < 1000; i++) {
						launchAgent(new Agent(), true);
						pause(100);
					}
				}
//				pause(1000000);
			}
		});
	}
}