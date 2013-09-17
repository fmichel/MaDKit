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
package madkit.boot.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class NullArgTest extends JunitMadkit {

	@Test
	public void desktopOn() {
		mkArgs = null;
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertTrue(BooleanOption.desktop.isActivated(getMadkitConfig()));
			}
		});
	}
	
//	@Test
//	public void noDesktop(){
//	System.err.println(java.lang.management.ManagementFactory.getRuntimeMXBean().getName());
//	Madkit m = new Madkit(BooleanOption.desktop.toString(), "false",Option.launchAgents.toString(),Agent.class.getName());
//	try {
//		Thread.sleep(1000000);
//	} catch (InterruptedException e) {
//		e.printStackTrace();
//	}
//	m.doAction(KernelAction.LAUNCH_NETWORK);
//		}

	@Test
	public void LogLevels() {
		mkArgs = null;
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals("OFF", getMadkitProperty(LevelOption.kernelLogLevel.name()));
				assertEquals("OFF", getMadkitProperty(LevelOption.guiLogLevel.name()));
				assertEquals("INFO", getMadkitProperty(LevelOption.networkLogLevel.name()));
				assertEquals("INFO", getMadkitProperty(LevelOption.madkitLogLevel.name()));
				assertEquals("INFO", getMadkitProperty(LevelOption.agentLogLevel.name()));
				assertEquals("FINE", getMadkitProperty(LevelOption.warningLogLevel.name()));
				assertEquals("INFO", getMadkitProperty(LevelOption.madkitLogLevel.name()));
			}
		});
	}
}
