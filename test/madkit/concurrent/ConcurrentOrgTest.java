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
package madkit.concurrent;

import java.util.logging.Level;

import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.9
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class ConcurrentOrgTest extends JunitMadkit {
	private int testSize = 4000;

	@Test
	public void noConflictTest() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(),Level.OFF.toString());
//		addMadkitArgs(LevelOption.kernelLogLevel.toString(),Level.ALL.toString());
		launchTest(new Agent() {
			@Override
			protected void activate() {
				for (int i = 0; i < testSize; i++) {
					launchAgent(new ConcurrentTestAgent(), 0);
				}
				System.err.println("everything launched");
				pause(15000);
			}
		});
	}

	@Test
	public void randomTest() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(),Level.OFF.toString());
//		addMadkitArgs(LevelOption.kernelLogLevel.toString(),Level.ALL.toString());
		launchTest(new Agent() {
			@Override
			protected void activate() {
				for (int i = 0; i < testSize; i++) {
					launchAgent(new ConcurrentTestAgentBis(), 0);
				}
				System.err.println("everything launched");
				pause(15000);
			}
		});
	}

}